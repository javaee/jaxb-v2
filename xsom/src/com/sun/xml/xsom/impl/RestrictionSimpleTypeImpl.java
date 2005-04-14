/*
 * @(#)$Id: RestrictionSimpleTypeImpl.java,v 1.1 2005-04-14 22:06:27 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import org.xml.sax.Locator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RestrictionSimpleTypeImpl extends SimpleTypeImpl implements XSRestrictionSimpleType {

    public RestrictionSimpleTypeImpl( SchemaImpl _parent,
        AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa,
        String _name, boolean _anonymous,
        Ref.SimpleType _baseType ) {
            
        super( _parent, _annon, _loc, _fa, _name, _anonymous, _baseType );
    }


    private final List<XSFacet> facets = new ArrayList<XSFacet>();
    public void addFacet( XSFacet facet ) {
        facets.add(facet);
    }
    public Iterator<XSFacet> iterateDeclaredFacets() {
        return facets.iterator();
    }

    public Collection<? extends XSFacet> getDeclaredFacets() {
        return facets;
    }

    public XSFacet getDeclaredFacet( String name ) {
        int len = facets.size();
        for( int i=0; i<len; i++ ) {
            XSFacet f = facets.get(i);
            if(f.getName().equals(name))
                return f;
        }
        return null;
    }

    public XSFacet getFacet( String name ) {
        XSFacet f = getDeclaredFacet(name);
        if(f!=null)     return f;
        
        // none was found on this datatype. check the base type.
        return getSimpleBaseType().getFacet(name);
    }

    public XSVariety getVariety() { return getSimpleBaseType().getVariety(); }
    
    public void visit( XSSimpleTypeVisitor visitor ) {
        visitor.restrictionSimpleType(this);
    }
    public Object apply( XSSimpleTypeFunction function ) {
        return function.restrictionSimpleType(this);
    }

    public boolean isRestriction() { return true; }
    public XSRestrictionSimpleType asRestriction() { return this; }
}
