/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.bind.marshaller;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.helpers.ValidationEventImpl;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.xml.bind.serializer.AbortSerializationException;
import com.sun.xml.bind.serializer.NamespaceContext2;
import com.sun.xml.bind.serializer.NamespaceContextImpl;
import com.sun.xml.bind.serializer.PrefixCallback;
import com.sun.xml.bind.serializer.XMLSerializable;
import com.sun.xml.bind.serializer.XMLSerializer;

/**
 * XMLSerializer that produces SAX2 events.
 * 
 * To marshal an object, create an instance of SAXMarshaller
 * and call the serializeElements method of the XMLSerializable
 * object that you want to marshal.
 * 
 * @author  Kohsuke Kawaguchi
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class SAXMarshaller implements XMLSerializer
{
    /**
     * "Attributes" object that is passed to the startElement event.
     * One object is reused throughout the marshalling.
     */
    private final AttributesImpl attributes = new AttributesImpl();
 
    /** This object receives SAX2 events. */
    private final ContentHandler writer;
    
    /** Marshaller object to which this object belongs. */
    private final MarshallerImpl owner;
    
    /**
     * Creates a marshalling context by designating the ContentHandler
     * that receives generated SAX2 events.
     */
    public SAXMarshaller( ContentHandler _writer, MarshallerImpl _owner ) {
        this.writer = _writer;
        this.owner = _owner;
    }
    
    /** namespace context. */
    private final NamespaceContextImpl nsContext = new NamespaceContextImpl();
    
    public NamespaceContext2 getNamespaceContext() { return nsContext; }
    
    //
    //
    // name stack
    //
    //
    
    /** Element name stack implemented as an array of (uri,local) pairs. */
    private String[] elementStack = new String[16];;
    private int elementLen=0;
    
    
    
    private void pushElement( String uri, String local ) {
        if(elementStack.length==elementLen) {
            // reallocate buffer
            String[] buf = new String[elementStack.length*2];
            System.arraycopy( elementStack, 0, buf, 0, elementStack.length );
            elementStack = buf;
        }
        elementStack[elementLen++] = uri;
        elementStack[elementLen++] = local;
    }
    
    private void popElement() { elementLen-=2;  }
    
    private String getCurrentElementUri()   { return elementStack[elementLen-2]; }
    private String getCurrentElementLocal() { return elementStack[elementLen-1]; }
    
    
    
    
    
    /**
     * Starts marshalling of an element.
     * Calling this method will push the internal state into the
     * internal stack.
     */
    public void startElement( String uri, String local ) throws SAXException {
        writePendingText();
        nsContext.startElement();
        pushElement(uri,local); // memorize element name
    }
    
    
    private final PrefixCallback startPrefixCallback = new PrefixCallback() {
        public void onPrefixMapping( String prefix, String nsUri ) throws SAXException {
            writer.startPrefixMapping(prefix,nsUri);
        }
    };
    private final PrefixCallback endPrefixCallback = new PrefixCallback() {
        public void onPrefixMapping( String prefix, String nsUri ) throws SAXException {
            writer.endPrefixMapping(prefix);
        }
    };
    
    
    /**
     * Switches to the "marshal child texts/elements" mode.
     * This method has to be called after the 1st pass is completed.
     */
    public void endAttributes() throws SAXException {
        // calculate QName of the element
        String uri = getCurrentElementUri();
        String local = getCurrentElementLocal();
        
        String prefix = nsContext.declareNamespace(uri,false);
        String qname = local;
        if(prefix!=null)    qname = prefix+':'+qname;

        // fire startPrefixMapping events
        nsContext.iterateDeclaredPrefixes(startPrefixCallback);
        
        // fire the startElement event
        writer.startElement( uri, local, qname, attributes );
        
        
        // reset attributes
        attributes.clear();
        
        // prepare to collect texts
        _assert(textBuf==null);
        textBuf = new StringBuffer();
    }
    
    /**
     * Ends marshalling of an element.
     * Pops the internal stack.
     */
    public void endElement() throws SAXException {
        writePendingText();
        
        String uri = getCurrentElementUri();
        String local = getCurrentElementLocal();
        
        String prefix = nsContext.getPrefix(uri);
        String qname = local;
        if(prefix!=null)    qname = prefix+':'+qname;
        
        writer.endElement( uri, local, qname );

        // pop namespace bindings and
        // fire endPrefixMapping events
        nsContext.iterateDeclaredPrefixes(endPrefixCallback);
        
        popElement();
        
        // prepare to collect texts
        _assert(textBuf==null);
        textBuf = new StringBuffer();
        
        nsContext.endElement();
    }
    
    
    /** Buffer for collecting characters. */
    private StringBuffer textBuf = new StringBuffer();
    
    /**
     * Marshalls text.
     * 
     * <p>
     * This method can be called (i) after the startAttribute method
     * and (ii) before the endAttribute method, to marshal attribute values.
     * If the method is called more than once, those texts are considered
     * as separated by whitespaces. For example,
     * 
     * <pre>
     * c.startAttribute();
     * c.text("abc");
     * c.text("def");
     * c.endAttribute("","foo");
     * </pre>
     * 
     * will generate foo="abc def".
     * 
     * <p>
     * Similarly, this method can be called after the endAttributes
     * method to marshal texts inside elements. The same rule about
     * multiple invokations apply to this case, too. For example,
     * 
     * <pre>
     * c.startElement("","foo");
     * c.endAttributes();
     * c.text("abc");
     * c.text("def");
     *   c.startElement("","bar");
     *   c.endAttributes();
     *   c.endElement();
     * c.text("ghi");
     * c.endElement();
     * </pre>
     * 
     * will generate <code>&lt;foo>abc def&lt;bar/>ghi&lt;/foo></code>.
     */
    public void text( String text ) throws SAXException {
        // If the assertion fails, it must be a bug of xjc.
        // right now, we are not expecting the text method to be called.
        _assert(textBuf!=null);
        
        if(text==null) {
            ValidationEvent ev = new ValidationEventImpl(
                ValidationEvent.ERROR,
                Messages.format(Messages.ERR_MISSING_OBJECT), null,
                new NullPointerException() );
        
            reportError(ev);
            return;
        }
    
        if(textBuf.length()!=0)
            textBuf.append(' ');
        textBuf.append(text);
    }
    
    /**
     * Writes pending text (characters inside elements) to the writer.
     * This method is called from startElement and endElement.
     */
    private void writePendingText() throws SAXException {
        // assert(textBuf!=null);
        if(textBuf.length()!=0)
            writer.characters( textBuf.toString().toCharArray(), 0, textBuf.length() );
        textBuf = null;
    }
    
    /**
     * Starts marshalling of an attribute.
     * 
     * The marshalling of an attribute will be done by
     * <ol>
     *  <li>call the startAttribute method
     *  <li>call the text method (several times if necessary)
     *  <li>call the endAttribute method
     * </ol>
     * 
     * No two attributes can be marshalled at the same time.
     * Note that the whole attribute marshalling must be happened
     * after the startElement method and before the endAttributes method.
     */
    public void startAttribute( String uri, String local ) {
        _assert(textBuf==null);
        // allocate a buffer to collect attribute value
        textBuf = new StringBuffer();
        
        // remember the attribute name. We'll use this value later.
        this.attNamespaceUri = uri;
        this.attLocalName = local;
    }
    
    // used to keep attribute names until the endAttribute method is called.
    private String attNamespaceUri;
    private String attLocalName;

    public void endAttribute() {
        // use CDATA as the attribute type. This preserves
        // successive processors to collapse whitespaces.
        // (we cannot prevent characters like #xD to be replaced to
        // #x20, though).
        //
        // strictly speaking, attribute value normalization should be
        // provessed by XML parser, so it's unclear whether XML writer
        // uses this type value.
        //
        // in any way, CDATA type is the safest choice here.
        
        String qname;
        if(attNamespaceUri.length()==0) {
            // default namespace. don't need prefix
            qname = attLocalName;
        } else {
            qname = nsContext.declareNamespace(attNamespaceUri,true)+':'+attLocalName;
        }

        attributes.addAttribute(attNamespaceUri,attLocalName,qname,"CDATA",textBuf.toString());
        // free text buffer
        textBuf = null;
    }
    
    // we are not interested in checking the consistency of ID/IDREF.
    public String onID   ( String value ) throws SAXException { return value; }
    public String onIDREF( String value ) throws SAXException { return value; }


    public void childAsElements( XMLSerializable o ) throws SAXException {
        if(o==null) {
            // if null is passed, it usually means that the content tree object
            // doesn't have some of its required property.
            missingObjectError();
            // as a marshaller, we should be generous, so we'll continue to marshal
            // this document by skipping this missing object.
            return;
        }
        
        o.serializeElements(this);
    }
    
    public void childAsAttributes( XMLSerializable o ) throws SAXException {
        if(o==null) {
            missingObjectError();
            return;
        }
        o.serializeAttributes(this);
    }
    
    public void childAsAttributeBodies( XMLSerializable o ) throws SAXException {
        if(o==null) {
            missingObjectError();
            return;
        }
        o.serializeAttributeBodies(this);
    }
    
    
    public void reportError( ValidationEvent ve ) throws AbortSerializationException {
        ValidationEventHandler handler;
        
        try {
            handler = owner.getEventHandler();
        } catch( JAXBException e ) {
            throw new AbortSerializationException(e);
        }
        
        if(!handler.handleEvent(ve))
            throw new AbortSerializationException(ve.getMessage());
    }
    
    
    // TODO: probably we should have WrappedSAXException here,
    // so that we can throw MarshalException directly from this method.
    //
    // TODO: unless we change the method signature of childAsXXXX and take the parent object
    // as one of the parameters, we can't report any location associated with it.
    private void missingObjectError() throws SAXException {
        ValidationEvent ev = new ValidationEventImpl(
            ValidationEvent.ERROR, Messages.format(Messages.ERR_MISSING_OBJECT), null );
        
        reportError(ev);
    }
    
    
    private static void _assert( boolean b ) {
        if(!b)
            throw new InternalError( 
                Messages.format( Messages.ASSERT_FAILED ) );
    }
}
