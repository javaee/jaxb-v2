/*
 * @(#)$Id: AssociationMap.java,v 1.1 2005-04-15 20:03:52 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.bind.v2;

import java.util.HashMap;
import java.util.Map;

/**
 * Bi-directional map between elements, inner peers,
 * and outer peers.
 * 
 * <p>
 * TODO: this should be rewritten for efficiency.
 * 
 * @since 2.0
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class AssociationMap<XmlNode> {
    public final static class Entry<XmlNode> {
        /** XML element. */
    	private XmlNode element;
        /** inner peer, or null. */
        private Object inner;
        /** outer peer, or null. */
        private Object outer;
        
        public XmlNode element() {
        	return element;
        }
        public Object inner() {
        	return inner;
        }
        public Object outer() {
        	return outer;
        }
    }
    
    private final Map<XmlNode,Entry<XmlNode>> byElement = new HashMap<XmlNode,Entry<XmlNode>>();
    private final Map<Object,Entry<XmlNode>> byPeer = new HashMap<Object,Entry<XmlNode>>();
    
    /** Records the new element&lt;->inner peer association. */
    public void addInner( XmlNode element, Object inner ) {
        Entry<XmlNode> e = byElement.get(element);
        if(e!=null) {
        	if(e.inner!=null)
                byPeer.remove(e.inner);
            e.inner = inner;
        } else {
        	e = new Entry<XmlNode>();
            e.element = element;
            e.inner = inner;
        }
        
        byElement.put(element,e);
        
        Entry<XmlNode> old = byPeer.put(inner,e);
        if(old!=null) {
            if(old.outer!=null)
                byPeer.remove(old.outer);
            if(old.element!=null)
                byElement.remove(old.element);
        }
    }
    
    /** Records the new element&lt;->outer peer association. */
    public void addOuter( XmlNode element, Object outer ) {
        Entry<XmlNode> e = byElement.get(element);
        if(e!=null) {
            if(e.outer!=null)
                byPeer.remove(e.outer);
            e.outer = outer;
        } else {
            e = new Entry<XmlNode>();
            e.element = element;
            e.outer = outer;
        }
        
        byElement.put(element,e);
        
        Entry<XmlNode> old = byPeer.put(outer,e);
        if(old!=null) {
            old.outer=null;
            
            if(old.inner==null)
                // remove this entry
                byElement.remove(old.element);
        }
    }
    
    public Entry<XmlNode> byElement( Object e ) {
        return byElement.get(e);
    }
    
    public Entry<XmlNode> byPeer( Object o ) {
        return byElement.get(o);
    }
    
    public Object getInnerPeer( XmlNode element ) {
        Entry e = byElement(element);
        if(e==null)     return null;
        else            return e.inner;
    }
    
    public Object getOuterPeer( XmlNode element ) {
        Entry e = byElement(element);
        if(e==null)     return null;
        else            return e.outer;
    }
}
