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

package com.sun.xml.bind.unmarshaller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEvent;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.bind.TypeRegistry;
import com.sun.xml.bind.util.AttributesImpl;

/**
 * Implementation of {@link UnmarshallerHandler}.
 * 
 * This object converts SAX events into unmarshaller events and
 * cooridnates the entire unmarshalling process.
 *
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com>Kohsuke KAWAGUCHI</a>
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class SAXUnmarshallerHandlerImpl
    implements SAXUnmarshallerHandler, UnmarshallingContext
{
    /**
     * This flag is set to true at the startDocument event
     * and false at the endDocument event.
     * 
     * Until the first document is unmarshalled, we don't
     * want to return an object. So this variable is initialized
     * to true.
     */
    private boolean isUnmarshalInProgress = true;
    
    
    public SAXUnmarshallerHandlerImpl( UnmarshallerImpl _parent, TypeRegistry _reg ) {
        this.parent = _parent;
        typeRegistry = _reg;
        startPrefixMapping("",""); // by default, the default ns is bound to "".
     }
    
    private final TypeRegistry typeRegistry;
    public TypeRegistry getTypeRegistry() { return typeRegistry; }
    
    public void startDocument() throws SAXException {
        // reset the object
        result = null;
        handlerLen=0;
        patchers=null;
        patchersLen=0;
        aborted = false;
        isUnmarshalInProgress = true;
        
        attStack.clear();
    }
    
    public void endDocument() throws SAXException {
        runPatchers();
        isUnmarshalInProgress = false;
    }
    
    public void startElement( String uri, String local, String qName, Attributes atts )
            throws SAXException {
        
        // symbolize
        uri = uri.intern();
        local = local.intern();
        qName = qName.intern();
        
        try {
            if(result==null) {
                // this is the root element.
                // create a root object and start unmarshalling
                result = typeRegistry.createRootElement(uri,local);
                if(result==null) {
                    // the registry doesn't know about this element.
                    throw new SAXParseException(
                        Messages.format( Messages.UNEXPECTED_ROOT_ELEMENT, qName ),
                        getLocator() );
                }

                UnmarshallingEventHandler unmarshaller = result.getUnmarshaller(this);
                pushContentHandler(unmarshaller,0);
            }
        
            processText(true);
        
            getCurrentHandler().enterElement(uri,local,atts);
        } catch( UnreportedException e ) {
            // any SAXException encountered at this point has not been
            // reported to the client. Report it.
            reportAndThrow(e);
        }
    }

    public final void endElement( String uri, String local, String qname )
            throws SAXException {
        
        // symbolize
        uri = uri.intern();
        local = local.intern();
        qname = qname.intern();
        
        try {
            processText(false);
            getCurrentHandler().leaveElement(uri,local);
        } catch( UnreportedException e ) {
            // any SAXException encountered at this point has not been
            // reported to the client. Report it.
            reportAndThrow(e);
        }
    }
    
    
    
    
    
    /** Root object that is being unmarshalled. */
    private UnmarshallableObject result;
    public Object getResult() throws UnmarshalException {
        if(isUnmarshalInProgress)
            throw new IllegalStateException();
        
        if(!aborted)       return result;
        
        // there was an error.
        throw new UnmarshalException((String)null);
    }

    
    
//
//
// handler stack maintainance
//
//
    private UnmarshallingEventHandler[] handlers = new UnmarshallingEventHandler[16];
    private int[] mementos = new int[16];
    private int handlerLen=0;
    
    public void pushContentHandler( UnmarshallingEventHandler handler, int memento ) {
        if(handlerLen==handlers.length) {
            // expand buffer
            UnmarshallingEventHandler[] h = new UnmarshallingEventHandler[handlerLen*2];
            int[] m = new int[handlerLen*2];
            System.arraycopy(handlers,0,h,0,handlerLen);
            System.arraycopy(mementos,0,m,0,handlerLen);
            handlers = h;
            mementos = m;
        }
        handlers[handlerLen] = handler;
        mementos[handlerLen] = memento;
        handlerLen++;
    }
    
    public void popContentHandler() throws UnreportedException {
        handlerLen--;
        handlers[handlerLen]=null;  // this handler is removed
        getCurrentHandler().leaveChild(mementos[handlerLen]);
    }
    /**
     * @deprecated
     */
    public ContentHandlerEx getCurrentHandler() {
        return (ContentHandlerEx)getCurrentEventHandler();
    }

    public UnmarshallingEventHandler getCurrentEventHandler() {
        return handlers[handlerLen-1];
    }


//
//
// text handling
//
//    
    private StringBuffer buffer = new StringBuffer();
    
    protected void consumeText( String str, boolean ignorable ) throws UnreportedException {
        if(getCurrentHandler().isListState()) {
            // in list state, we don't need to care about whitespaces.
            // if the text is all whitespace, this won't generate a text event,
            // so it would be just fine.
            
            StringTokenizer tokens = new StringTokenizer(str);
            while(tokens.hasMoreTokens())
                // the handler can be switched during the text processing,
                // so the current handler has to be obtained inside the loop
                getCurrentHandler().text(tokens.nextToken());
        } else {
            if(ignorable && str.trim().length()==0)
                // if we are allowed to ignore text and
                // the text is ignorable, ignore.
                return;
            
            // otherwise perform a transition by this token.
            getCurrentHandler().text(str);
        }
    }
    private void processText( boolean ignorable ) throws UnreportedException {
        consumeText(buffer.toString(),ignorable);
        
        // avoid excessive object allocation, but also avoid
        // keeping a huge array inside StringBuffer.
        if(buffer.length()<1024)    buffer.setLength(0);
        else                        buffer = new StringBuffer();
    }
    
    public final void characters( char[] buf, int start, int len ) {
        buffer.append(buf,start,len);
    }

    public final void ignorableWhitespace( char[] buf, int start, int len ) {
        characters(buf,start,len);
    }



    
//
//
// namespace binding maintainance
//
//
    private String[] nsBind = new String[16];
    private int nsLen=0;
    
    // in the current scope, nsBind[0] - nsBind[idxStack[idxStackTop]-1]
    // are active.
    private int[] idxStack = new int[16];
    private int idxStackTop=0;
    
    public void startPrefixMapping( String prefix, String uri ) {
        if(nsBind.length==nsLen) {
            // expand the buffer
            String[] n = new String[nsLen*2];
            System.arraycopy(nsBind,0,n,0,nsLen);
            nsBind=n;
        }
        nsBind[nsLen++] = prefix;
        nsBind[nsLen++] = uri;
    }
    public void endPrefixMapping( String prefix ) {
        nsLen-=2;
    }
    public String resolveNamespacePrefix( String prefix ) {
        if(prefix.equals("xml"))
            return "http://www.w3.org/XML/1998/namespace";
        
        for( int i=idxStack[idxStackTop]-2; i>=0; i-=2 ) {
            if(prefix.equals(nsBind[i]))
                return nsBind[i+1];
        }
        return null;
    }
    //
    //  NamespaceContext2 implementation 
    //
    public Iterator getPrefixes(String uri) {
        // TODO: could be implemented much faster
        // wrap it into unmodifiable list so that the remove method
        // will throw UnsupportedOperationException.
        return Collections.unmodifiableList(
            getAllPrefixesInList(uri)).iterator();
    }
    
    private List getAllPrefixesInList(String uri) {
        List a = new ArrayList();
        
        if( uri.equals(XMLConstants.XML_NS_URI) ) {
            a.add(XMLConstants.XML_NS_PREFIX);
            return a;
        }
        if( uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) ) {
            a.add(XMLConstants.XMLNS_ATTRIBUTE);
            return a;
        }
        if( uri==null )
            throw new IllegalArgumentException();
          
        for( int i=nsLen-2; i>=0; i-=2 )
            if(uri.equals(nsBind[i+1]))
                if( getNamespaceURI(nsBind[i]).equals(nsBind[i+1]) )
                    // make sure that this prefix is still effective.
                    a.add(nsBind[i]);
         
        return a;
    }

    public String getPrefix(String uri) {
        if( uri.equals(XMLConstants.XML_NS_URI) )
            return XMLConstants.XML_NS_PREFIX;
        if( uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) )
            return XMLConstants.XMLNS_ATTRIBUTE;
        if( uri==null )
            throw new IllegalArgumentException();
          
        for( int i=idxStack[idxStackTop]-2; i>=0; i-=2 )
            if(uri.equals(nsBind[i+1]))
                if( getNamespaceURI(nsBind[i]).equals(nsBind[i+1]) )
                    // make sure that this prefix is still effective.
                    return nsBind[i];
         
        return null;
    }

     public String getNamespaceURI(String prefix) {
         if( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) )
             return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
         if( prefix==null )
             throw new IllegalArgumentException();
        
         return resolveNamespacePrefix(prefix);
     }

//
//
// Attribute handling
//
//
    // TODO: implementation can be more efficient
    private final Stack attStack = new Stack();
    
    public void pushAttributes( Attributes atts ) {
        // since Attributes object is mutable, it is criticall important
        // to make a copy.
        // also symbolize attribute names
        AttributesImpl a = new AttributesImpl();
        for( int i=0; i<atts.getLength(); i++ )
            a.addAttribute(
                atts.getURI(i).intern(),
                atts.getLocalName(i).intern(),
                atts.getQName(i).intern(),
                atts.getType(i),
                atts.getValue(i) );
        
        // <foo xsi:nil="false">some value</foo> is a valid fragment, however
        // we need a look ahead to correctly handle this case.
        // (because when we process @xsi:nil, we don't know what the value is,
        // and by the time we read "false", we can't cancel this attribute anymore.)
        //
        // as a quick workaround, we remove @xsi:nil if the value is false.
        int idx = a.getIndex("http://www.w3.org/2001/XMLSchema-instance","nil");
        if(idx!=-1) {
            String v = a.getValue(idx).trim();
            if(v.equals("false") || v.equals("0"))
                a.removeAttribute(idx);
        }
        
        attStack.push(a);
        
        // start a new namespace scope
        if( ++idxStackTop==idxStack.length ) {
            // reallocation
            int[] newBuf = new int[idxStack.length*2];
            System.arraycopy(idxStack,0,newBuf,0,idxStack.length);
            idxStack = newBuf;
        }
        idxStack[idxStackTop] = nsLen;
    }
    public void popAttributes() {
        attStack.pop();
        
        idxStackTop--;
    }
    public Attributes getUnconsumedAttributes() {
        return (Attributes)attStack.peek();
    }
    public int getAttribute( String uri, String local ) {
        // Consider a class that corresponds to the root element.
        // if this class has a transition from final state by an attribute,
        // then this attribute transitions are checked when the final
        // leaveElement event is consumed.
        //
        // to handle this case, return "not found" if there is no active
        // attribute scope
        if(attStack.isEmpty())  return -1;
        
        Attributes a = (Attributes)attStack.peek();
        
        return a.getIndex(uri,local);
    }
    public void consumeAttribute( int idx ) throws UnreportedException {
        AttributesImpl a = (AttributesImpl)attStack.peek();
        
        String uri = a.getURI(idx).intern();
        String local = a.getLocalName(idx).intern();
        String value = a.getValue(idx).intern();

        // mark the attribute as consumed
        // we need to remove the attribute before we process it
        // because the event handler might access attributes.
        a.removeAttribute(idx);
        
        
        getCurrentHandler().enterAttribute(uri,local);
        consumeText(value,false);
        getCurrentHandler().leaveAttribute(uri,local);
    }

//
//
// ID/IDREF related code
//
//
    /**
     * Submitted patchers in the order they've submitted.
     * Many XML vocabulary doesn't use ID/IDREF at all, so we
     * initialize it with null.
     */
    private Runnable[] patchers = null;
    private int patchersLen = 0;
    
    public void addPatcher( Runnable job ) {
        // re-allocate buffer if necessary
        if( patchers==null )
            patchers = new Runnable[32];
        if( patchers.length == patchersLen ) {
            Runnable[] buf = new Runnable[patchersLen*2];
            System.arraycopy(patchers,0,buf,0,patchersLen);
            patchers = buf;
        }
        patchers[patchersLen++] = job;
    }
    
    /** Executes all the patchers. */
    private void runPatchers() {
        if( patchers!=null ) {
            for( int i=0; i<patchersLen; i++ )
                patchers[i].run();
        }
    }

    /** Records ID->Object map. */
    private Hashtable idmap = null;

    public String addToIdTable( String id ) {
        if(idmap==null)     idmap = new Hashtable();
        idmap.put( id, getCurrentHandler().owner() );
        return id;
    }
    
    public UnmarshallableObject getObjectFromId( String id ) {
        if(idmap==null)     return null;
        return (UnmarshallableObject)idmap.get(id);
    }
    


//
//
// Other SAX callbacks
//
//
    public void skippedEntity( String name ) {
        // TODO: throw an exception, perhaps?
    }
    public void processingInstruction( String target, String data ) {
        // just ignore
    }
    public void setDocumentLocator( Locator loc ) {
        locator = loc;
    }
    public Locator getLocator() { return locator; }
    
    private Locator locator;


//
//
// error handling
//
//
    private final UnmarshallerImpl parent;
    private boolean aborted = false;
    
    public boolean handleEvent(ValidationEvent event) {
        try {
            // if the handler says "abort", we will not return
            // the object.
            boolean recover = parent.getEventHandler().handleEvent(event);
            if(!recover)    aborted = true;
            return recover;
        } catch( JAXBException e ) {
            // we are not allowed to throw an exception from this method.
            return false;
        } catch( RuntimeException re ) {
            // if client event handler causes a runtime exception, then we
            // have to return false.
            return false;
        }
    }
    
    public void reportAndThrow( UnreportedException e ) throws SAXException {
        handleEvent(e.createValidationEvent());
        
        // rewrap it into UnmarshalException.
        // we further re-wrap it into SAXException again so that
        // it can be tunneled through SAX Parser.
        throw new SAXException( e.createUnmarshalException() );
    }
  
//
//
// ValidationContext implementation
//
//
    public String getBaseUri() { return null; }
    public boolean isUnparsedEntity(String s) { return true; }
    public boolean isNotation(String s) { return true; }


//
//
// debug trace methods
//
//
    private Tracer tracer;
    public void setTracer( Tracer t ) {
        this.tracer = t;
    }
    public Tracer getTracer() {
        if(tracer==null)
            tracer = new Tracer.Standard();
        return tracer;
    }
    

}
