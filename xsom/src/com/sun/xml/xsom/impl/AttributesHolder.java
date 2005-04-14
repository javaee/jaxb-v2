/*
 * @(#)$Id: AttributesHolder.java,v 1.1 2005-04-14 22:06:24 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeUse;

public abstract class AttributesHolder extends DeclarationImpl {
    
    protected AttributesHolder( SchemaImpl _parent, AnnotationImpl _annon,
        Locator loc, ForeignAttributesImpl _fa, String _name, boolean _anonymous ) {
        
        super(_parent,_annon,loc,_fa,_parent.getTargetNamespace(),_name,_anonymous);
    }
    
    /** set the local wildcard. */
    public abstract void setWildcard(WildcardImpl wc);
    
    /**
     * Local attribute use.
     * It has to be {@link TreeMap} or otherwise we cannot guarantee
     * the order of iteration.
     */
    protected final Map attributes = new TreeMap(UName.comparator);
    public void addAttributeUse( UName name, AttributeUseImpl a ) {
        attributes.put( name, a );
    }
    /** prohibited attributes. */
    protected final Set prohibitedAtts = new HashSet();
    public void addProhibitedAttribute( UName name ) {
        prohibitedAtts.add(name);
    }
    public Iterator iterateAttributeUses() {
        // TODO: this is fairly inefficient
        List v = new ArrayList();
        v.addAll(attributes.values());
        Iterator itr = iterateAttGroups();
        while(itr.hasNext()) {
            Iterator jtr = ((XSAttGroupDecl)itr.next()).iterateAttributeUses();
            while(jtr.hasNext())
                v.add(jtr.next());
        }
        return v.iterator();
    }

    public XSAttributeUse getDeclaredAttributeUse( String nsURI, String localName ) {
        return (XSAttributeUse)attributes.get(new UName(nsURI,localName));
    }
    
    public Iterator iterateDeclaredAttributeUses() {
        return attributes.values().iterator();
    }

    
    /** {@link Ref.AttGroup}s that are directly refered from this. */
    protected final Set attGroups = new HashSet();
    
    public void addAttGroup( Ref.AttGroup a ) { attGroups.add(a); }
    
    // Iterates all AttGroups which are directly referenced from this component
    // this does not iterate att groups referenced from the base type
    public Iterator iterateAttGroups() {
        return new Iterator() {
            private final Iterator itr = attGroups.iterator();
            public boolean hasNext() { return itr.hasNext(); }
            public Object next() {
                return ((Ref.AttGroup)itr.next()).get();
            }
            public void remove() { itr.remove(); }
        };
    }    
}
