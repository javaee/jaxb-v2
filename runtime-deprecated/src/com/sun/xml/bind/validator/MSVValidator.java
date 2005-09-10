/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.bind.validator;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.relaxng.datatype.Datatype;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.msv.grammar.IDContextProvider;
import com.sun.msv.util.LightStack;
import com.sun.msv.util.StartTagInfo;
import com.sun.msv.util.StringRef;
import com.sun.msv.verifier.Acceptor;
import com.sun.xml.bind.RIElement;
import com.sun.xml.bind.serializer.*;
import com.sun.xml.bind.serializer.NamespaceContext2;
import com.sun.xml.bind.serializer.XMLSerializable;
import com.sun.xml.bind.serializer.XMLSerializer;

/**
 * XMLSerializer that calls the native interface of MSV and performs validation.
 * Used in a pair with a ValidationContext.
 * 
 * @author  Kohsuke Kawaguchi
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class MSVValidator implements XMLSerializer, IDContextProvider
{
    /** Current acceptor in use. */
    private Acceptor acceptor;
    
    /** Context object that coordinates the entire validation effort. */
    private final ValidationContext context;
    
    /** The object which we are validating. */
    private final ValidatableObject target;
    
    /**
     * Acceptor stack. Whenever an element is found, the current acceptor is
     * pushed to the stack and new one is created.
     * 
     * LightStack is a light-weight stack implementation
     */
    private final LightStack stack = new LightStack();

    public NamespaceContext2 getNamespaceContext() {
        return context.getNamespaceContext();
    }
    
    /**
     * To use this class, call the static validate method.
     */
    private MSVValidator( ValidationContext _ctxt, ValidatableObject vo ) {
        acceptor = vo.createRawValidator().createAcceptor();
        context = _ctxt;
        target = vo;
    }
    
    /**
     * Validates the specified object and reports any error to the context.
     */
    public static void validate( ValidationContext context, ValidatableObject vo )
            throws SAXException {
        try {
            new MSVValidator(context,vo)._validate();
        } catch( RuntimeException e ) {
            // sometimes when a conversion between Java object and
            // lexical value fails, it may throw an exception like
            // NullPointerException or NumberFormatException.
            //
            // catch them and report them as an error.
            context.reportEvent(vo,e);
        }
    }
    
    /** performs the validation to the object specified in the constructor. */
    private void _validate() throws SAXException {
        // validate attributes
        target.serializeAttributes(this);
        
        endAttribute();
        
        // validate content model
        target.serializeElements(this);
        writePendingText();
        
        if(!acceptor.isAcceptState(null)) {
            // some elements are missing
            // report error
            StringRef ref = new StringRef();
            acceptor.isAcceptState(ref);
            context.reportEvent(target,ref.str);
        }
    }
    
    public void endAttributes() throws SAXException {
        // TODO: supply StartTagInfo by remembering all reported attributes
        if(!acceptor.onEndAttributes( null, null )) {
            // some required attributes are missing.
            // report a validation error
            // Note that we don't know which property of this object
            // causes this error.
            StringRef ref = new StringRef();
            StartTagInfo sti = new StartTagInfo(
                currentElementUri,currentElementLocalName,currentElementLocalName,
                emptyAttributes,this);
            acceptor.onEndAttributes( sti, ref );
            context.reportEvent(target,ref.str);
        }
    }
    
    /** stores text reported by the text method. */
    private StringBuffer buf = new StringBuffer();
        
    public final void text( String text ) throws SAXException {
        if(text==null) {
            reportMissingObjectError();
            return;
        }
        
        if(buf.length()!=0)
            buf.append(' ');
        buf.append(text);
    }
    
    private void reportMissingObjectError() throws SAXException {
        ValidationEvent ev = new ValidationEventImpl(
            ValidationEvent.ERROR,
            Messages.format(Messages.MISSING_OBJECT),
            new ValidationEventLocatorImpl(target),
            new NullPointerException() );
    
        reportError(ev);
    }
    

    // used to keep attribute names until the endAttribute method is called.
    private String attNamespaceUri;
    private String attLocalName;

    public void startAttribute( String uri, String local ) {
        // we will do the processing at the end element
        this.attNamespaceUri = uri;
        this.attLocalName = local;
    }
    
    public void endAttribute() throws SAXException {
        if(!acceptor.onAttribute( attNamespaceUri, attLocalName,
            attLocalName /* we don't have QName, so just use the local name */,
            buf.toString(),
            this, null, null )) {
            
            // either the name was incorrect (which is quite unlikely),
            // or the value was wrong.
            // report an error
            StringRef ref = new StringRef();
            acceptor.onAttribute( attNamespaceUri, attLocalName, attLocalName,
            buf.toString(), this, ref, null );
            
            context.reportEvent(target,ref.str);
        }
        
        buf = new StringBuffer();
    }
    
    private void writePendingText() throws SAXException {
        // assert(textBuf!=null);
        if(!acceptor.onText( buf.toString(), this, null, null )) {
            // this text is invalid.
            // report an error
            StringRef ref = new StringRef();
            acceptor.onText( buf.toString(), this, ref, null );
            context.reportEvent(target,ref.str);
        }
        
        if(buf.length()>1024)
            buf = new StringBuffer();
        else
            buf.setLength(0);
    }
    
    private String currentElementUri;
    private String currentElementLocalName;
    
    public void startElement( String uri, String local ) throws SAXException {
        writePendingText();
        
        context.getNamespaceContext().startElement();
        
        stack.push(acceptor);
        
        StartTagInfo sti = new StartTagInfo(uri,local,local,emptyAttributes,this);
        
        // we pass in an empty attributes, as there is just no way for us to
        // properly re-construct attributes. Fortunately, I know MSV is not using
        // attribute values, so this would work, but nevertheless this code is
        // ugly. This is one of the problems of the "middle" approach.
        Acceptor child = acceptor.createChildAcceptor( sti, null );
        if( child==null ) {
            // this element is invalid. probably, so this object is invalid
            // report an error
            StringRef ref = new StringRef();
            child = acceptor.createChildAcceptor( sti, ref );
            context.reportEvent(target,ref.str);
        }
        
        this.currentElementUri = uri;
        this.currentElementLocalName = local;
        
        acceptor = child;
    }
    
    public void endElement() throws SAXException {
        writePendingText();
        
        if(!acceptor.isAcceptState(null)) {
            // some required elements are missing
            // report error
            StringRef ref = new StringRef();
            acceptor.isAcceptState(ref);
            context.reportEvent(target,ref.str);
        }
        
        // pop the acceptor
        Acceptor child = acceptor;
        acceptor = (Acceptor)stack.pop();
        if(!acceptor.stepForward( child, null )) {
            // some required elements are missing.
            // report an error
            StringRef ref = new StringRef();
            acceptor.stepForward( child, ref );  // force recovery and obtain an error message.
            
            context.reportEvent(target,ref.str);
        }
        
        context.getNamespaceContext().endElement();
    }
    
    
    public void childAsAttributes( XMLSerializable o ) throws SAXException {
        // do nothing
        
        // either the onMarshallableObjectInElement method
        // or the onMarshallableObjectInAttributeBody method will be 
        // called for every content tree objects.
        //
        // so we don't need to validate an object within this method.
    }
    
    /** An empty <code>Attributes</code> object. */
    private static final AttributesImpl emptyAttributes = new AttributesImpl();
    
    /** namespace URI of dummy elements. TODO: allocate one namespace URI for this. */
    public static final String DUMMY_ELEMENT_NS =
        "http://java.sun.com/jaxb/xjc/dummy-elements";
    
    public void childAsElements( XMLSerializable o ) throws SAXException {
        final ValidatableObject vo = (ValidatableObject)o;

        if(vo==null) {
            reportMissingObjectError();
            return;
        }
       
        String intfName = vo.getPrimaryInterface().getName();
        intfName = intfName.replace('$','.');
        
        // if the object implements the RIElement interface,
        // add a marker attribute to the dummy element.
        //
        // For example, if the object is org.acme.impl.FooImpl,
        // the dummy element will look like
        // <{DUMMY_ELEMENT_NS}org.acme.Foo
        //          {<URI of this element>}:<local name of this element>="" />
        // 
        // This extra attribute is used to validate wildcards.
//        AttributesImpl atts;
//        if(o instanceof RIElement) {
//            RIElement rie = (RIElement)o;
//            atts = new AttributesImpl();
//            atts.addAttribute(
//                rie.____jaxb_ri____getNamespaceURI(),
//                rie.____jaxb_ri____getLocalName(),
//                rie.____jaxb_ri____getLocalName(),  // use local name as qname
//                "CDATA",
//                "");    // we don't care about the attribute value
//        } else
//            atts = emptyAttributes;
            
        
        // feed a dummy element to the acceptor.
        StartTagInfo sti = new StartTagInfo(
            DUMMY_ELEMENT_NS,
            intfName,
            intfName/*just pass the local name as QName.*/,
            emptyAttributes,
            this );
        
            
        Acceptor child = acceptor.createChildAcceptor(sti,null);
        if(child==null) {
            // some required elements were missing. report errors
            StringRef ref = new StringRef();
            child = acceptor.createChildAcceptor(sti,ref);
            context.reportEvent(target,ref.str);
        }
        
        if(o instanceof RIElement) {
            RIElement rie = (RIElement)o;
            if(!child.onAttribute(
                rie.____jaxb_ri____getNamespaceURI(),
                rie.____jaxb_ri____getLocalName(),
                rie.____jaxb_ri____getLocalName(),
                "",
                null, null, null ))
                
                // this object is not a valid member of the wildcard
                context.reportEvent(target,
                    Messages.format( Messages.INCORRECT_CHILD_FOR_WILDCARD,
                        rie.____jaxb_ri____getNamespaceURI(),
                        rie.____jaxb_ri____getLocalName() ));
        }
        
        child.onEndAttributes(sti,null);
        
        
        if(!acceptor.stepForward(child,null)) {
            // this can't be possible, as the dummy element was 
            // generated by XJC.
            throw new InternalError();
        }

        
        // we need a separate validator instance to validate a child object
        context.validate(vo);
        
    }
    
    public void childAsAttributeBodies( XMLSerializable o ) throws SAXException {
        /*
        Dirty quick hack. When we split a schema into fragments, basically
        every chlid object needs a place holder in the fragment
        (so that the parent schema fragment can correctly validate that the
        child objects are at their supposed places.)
        
        For example, cconsider the following schema:
        
        imagine:
        <class>
          <attribute>
            <list>
              <oneOrMore>
                <ref name="bar"/>
              </oneOrMore>
            </list>
          </attribute>
        </class>
        
        In our algorithm, the corresponding schema fragment will be:
        
        <class>
          <attribute>
            <list>
              <oneOrMore>
                <value>\u0000full.class.name.of.BarImpl</value>
              </oneOrMore>
            </list>
          </attribute>
        </class>
        
        If we find a child object inside an attribute
        (that's why we are in this method BTW),
        we generate a class name (with a special marker \u0000).
        */
        
        final ValidatableObject vo = (ValidatableObject)o;
        
        if(vo==null) {
            reportMissingObjectError();
            return;
        }
        
        // put a class name with a special marker \u0000. This char is an invalid
        // XML char, so sensible datatypes should reject this (although many
        // datatype implementations will accept it in actuality)
        text("\u0000"+vo.getPrimaryInterface().getName());

        // validate a child object
        context.validate(vo);
    }


    public void reportError( ValidationEvent e ) throws AbortSerializationException {
        context.reportEvent(target,e);
    }

//
//
// ID/IDREF validation
//
//
    public String onID( String value ) throws SAXException {
        return context.onID(target,value);
    }
    public String onIDREF( String value ) throws SAXException {
        return context.onIDREF(target,value);
    }

//
//  
// ValidationContext implementation. Used by MSV to obtain
// contextual information related to validation.
//
//
    public String getBaseUri() { return null; }
    public boolean isUnparsedEntity( String entityName ) {
        // abandon the validation of ENTITY type.
        return true;
    }
    public boolean isNotation( String notation ) {
        // abandon the validation of NOTATION type.
        return true;
    }
    public void onID( Datatype dt, String s ) {
        // ID/IDREF validation will be done by ourselves.
        // so we will not rely on the validator to perform this check.
        // because we will use multiple instances of validators, so 
        // they cannot check global consistency.
        
        // see onID/onIDREF of the ValidationContext.
    }
    public String resolveNamespacePrefix( String prefix ) {
        return context.getNamespaceContext().getNamespaceURI(prefix);
    }
    
}
