/*
 * @(#)$Id: AttributesHolder.java,v 1.3 2005-05-12 03:59:18 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeUse;
import org.xml.sax.Locator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Collection;
import java.util.AbstractSet;

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
    protected final Map<UName,AttributeUseImpl> attributes = new TreeMap<UName,AttributeUseImpl>(UName.comparator);
    public void addAttributeUse( UName name, AttributeUseImpl a ) {
        attributes.put( name, a );
    }
    /** prohibited attributes. */
    protected final Set<UName> prohibitedAtts = new HashSet<UName>();
    public void addProhibitedAttribute( UName name ) {
        prohibitedAtts.add(name);
    }
    public List<XSAttributeUse> getAttributeUses() {
        // TODO: this is fairly inefficient
        List<XSAttributeUse> v = new ArrayList<XSAttributeUse>();
        v.addAll(attributes.values());
        for( XSAttGroupDecl agd : getAttGroups() )
            v.addAll(agd.getAttributeUses());
        return v;
    }
    public Iterator<XSAttributeUse> iterateAttributeUses() {
        return getAttributeUses().iterator();
    }



    public XSAttributeUse getDeclaredAttributeUse( String nsURI, String localName ) {
        return attributes.get(new UName(nsURI,localName));
    }
    
    public Iterator<AttributeUseImpl> iterateDeclaredAttributeUses() {
        return attributes.values().iterator();
    }

    public Collection<AttributeUseImpl> getDeclaredAttributeUses() {
        return attributes.values();
    }


    /** {@link Ref.AttGroup}s that are directly refered from this. */
    protected final Set<Ref.AttGroup> attGroups = new HashSet<Ref.AttGroup>();
    
    public void addAttGroup( Ref.AttGroup a ) { attGroups.add(a); }
    
    // Iterates all AttGroups which are directly referenced from this component
    // this does not iterate att groups referenced from the base type
    public Iterator<XSAttGroupDecl> iterateAttGroups() {
        return new Iterator<XSAttGroupDecl>() {
            private final Iterator<Ref.AttGroup> itr = attGroups.iterator();
            public boolean hasNext() { return itr.hasNext(); }
            public XSAttGroupDecl next() {
                return itr.next().get();
            }
            public void remove() { itr.remove(); }
        };
    }

    public Set<XSAttGroupDecl> getAttGroups() {
        return new AbstractSet<XSAttGroupDecl>() {
            public Iterator<XSAttGroupDecl> iterator() {
                return iterateAttGroups();
            }

            public int size() {
                return attGroups.size();
            }
        };
    }
}
