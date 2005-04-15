/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.bind.serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.XMLConstants;

import org.xml.sax.SAXException;

/**
 * Implementation of the NamespaceContext2.
 * 
 * This class also provides several utility methods for
 * XMLSerializer-derived classes.
 * 
 * The startElement method and the endElement method need to be called
 * appropriately when used. See javadoc for those methods for details.
 * 
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class NamespaceContextImpl implements NamespaceContext2
{
    /** Sequence generator. */
    private int iota = 1;
    
    
    /**
     * Namespace declaration stack implemented as an array of (prefix,uri) pairs.
     * prefix==null means that it is the default namespace.
     */
    private String[] nsStack = new String[16];
    private int nsLen=0;

    /**
     * A stack of index to nsStack. Namespace declarations for
     * an element (elementStack[2i],elementStack[2i+1]) is
     * from nsStack[idxStack[i]] to nsStack[idxStack[i+1]]
     */
    private int[] idxStack = new int[16];
    private int idxLen=0;
    
    /**
     * This map is keyed by namespace URI and remembers the prefix
     * assigned to it. Since we don't reuse the same prefix for
     * different URIs, this map can be used to reduce the number
     * of distinct prefixes.
     */
    private final HashMap associationHistory = new HashMap();
    
    
    public NamespaceContextImpl() {
        // declare the default namespace binding
        // which are effective because of the way XML1.0 is made
        addBinding(null,"");
        addBinding( "xml",   XMLConstants.XML_NS_URI );
        addBinding( "xmlns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI );
    }

//
//
// public methods of MarshallingContext
//
//
    public String declareNamespace( String namespaceUri, boolean requirePrefix ) {
        // see if it's already declared
        for( int i = nsLen-2; i>=0; i-=2 ) {
            if( nsStack[i+1].equals(namespaceUri) ) {
                if( nsStack[i]!=null || !requirePrefix ) {
                    // assertion check: we use null to denote the default ns.
                    // using the empty string is therefore illegal.
                    if( "".equals(nsStack[i]) )
                        throw new InternalError();
                    return nsStack[i];
                }
            }
        }
        
        // this namespace URI is not bound.
        // assign a new prefix
        String prefix = assignPrefix(namespaceUri,requirePrefix);
        
        addBinding(prefix,namespaceUri);
        
        // assertion check: we use null to denote the default ns.
        // using the empty string is therefore illegal.
        if( "".equals(prefix) )
            throw new InternalError();
        
        return prefix;
    }

    public String getPrefix( String namespaceUri ) {
        // even through the method name is "getPrefix", we 
        // use this method to declare prefixes if necessary.
        return declareNamespace(namespaceUri,false);
    }
    
    /**
     * Obtains the namespace URI currently associated to the given prefix.
     * If no namespace URI is associated, return null.
     */
    public String getNamespaceURI( String prefix ) {
        // shall we throw an exception in case of error?
        
        for( int i = nsLen-2; i>=0; i-=2 ) {
            if( nsStack[i].equals(prefix) )
                return nsStack[i+1];
        }
        
        return null;
    }
    
    public Iterator getPrefixes( String namespaceUri ) {
        // it's important to search the array in the reverse order,
        // as the most recently declared namespace comes the last.
        for( int i = nsLen-2; i>=0; i-=2 ) {
            if( nsStack[i+1].equals(namespaceUri) ) {
                // we don't allocate two prefixes for the same namespace URI,
                // so if we find one, that's the one.
                ArrayList al = new ArrayList();
                al.add(nsStack[i]);
                return Collections.unmodifiableList(al).iterator();
            }
        }
        
        // wrap the collection into unmodifiable list so that the returned
        // iterator will throw UnsupportedOperationException from the remove method.
        
        return Collections.unmodifiableList(new ArrayList()).iterator();  // return the empty iterator
    }

    /**
     * Sets the current bindings aside and starts a new element context.
     * 
     * This method should be called at the beginning of the startElement method
     * of the Serializer implementation.
     */
    public void startElement() {
        // pushes the current value of nsLen into the stack.
        
        if(idxStack.length==idxLen) {
            // reallocate buffer
            int[] buf = new int[idxStack.length*2];
            System.arraycopy( idxStack, 0, buf, 0, idxStack.length );
            idxStack = buf;
        }
        idxStack[idxLen++] = nsLen;
    }
    
    /**
     * Ends the current element context and gets back to the parent context.
     * 
     * This method should be called at the end of the endElement method
     * of derived classes.
     */
    public void endElement() {
        // pop namespace declaration stack
        nsLen = getStartIndex();
        idxLen--;
    }

    
    
//
//
// utility methods for the derived classes
//
//
    /** Iterates all newly declared namespace prefixes for this element. */
    public void iterateDeclaredPrefixes( PrefixCallback callback ) throws SAXException {
        for( int i = getStartIndex(); i<nsLen; i+=2 ) {
            String p = nsStack[i];
            if(p!=null)
                callback.onPrefixMapping( p, nsStack[i+1] );
        }
    }
    
    
//
//
// implementation details
//
//
    /** Adds new binding to the stack. */
    private void addBinding( String prefix, String namespaceUri ) {
        // assertion check: prefix must be null, not "".
        if(prefix!=null && prefix.length()==0)     throw new InternalError();
        
        if(nsStack.length==nsLen) {
            // reallocate buffer
            String[] buf = new String[nsStack.length*2];
            System.arraycopy( nsStack, 0, buf, 0, nsStack.length );
            nsStack = buf;
        }
        
        nsStack[nsLen++] = prefix;
        nsStack[nsLen++] = namespaceUri;
    }
    
    
    private String assignPrefix( String namespaceUri, boolean prefixRequired ) {
        // TODO : allow users to specify their choice of namespace bindings.
        
        String prefix = (String)associationHistory.get(namespaceUri);
        if(prefix==null)
            // assign a new unique prefix
            associationHistory.put( namespaceUri, prefix= "ns"+(iota++) );
            
        return prefix;
    }

    /**
     * Gets the index of the first namespace declaration in the current element.
     * If there is no new declaration in this element, nsLen will be returned.
     */
    private int getStartIndex() { return idxStack[idxLen-1]; }
}
