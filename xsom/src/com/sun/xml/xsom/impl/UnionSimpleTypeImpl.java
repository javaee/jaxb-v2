/*
 * @(#)$Id: UnionSimpleTypeImpl.java,v 1.1 2005-04-14 22:06:27 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;

public class UnionSimpleTypeImpl extends SimpleTypeImpl implements XSUnionSimpleType
{
    public UnionSimpleTypeImpl( SchemaImpl _parent,
        AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa,
        String _name, boolean _anonymous,
        Ref.SimpleType[] _members ) {
        
        super(_parent,_annon,_loc,_fa,_name,_anonymous,
            _parent.parent.anySimpleType);
        
        this.memberTypes = _members;
    }
        
    private final Ref.SimpleType[] memberTypes;
    public XSSimpleType getMember( int idx ) { return memberTypes[idx].getSimpleType(); }
    public int getMemberSize() { return memberTypes.length; }
    
    public void visit( XSSimpleTypeVisitor visitor ) {
        visitor.unionSimpleType(this);
    }
    public Object apply( XSSimpleTypeFunction function ) {
        return function.unionSimpleType(this);
    }

    // union type by itself doesn't have any facet. */
    public XSFacet getFacet( String name ) { return null; }
    
    public XSVariety getVariety() { return XSVariety.LIST; }

    public boolean isUnion() { return true; }
    public XSUnionSimpleType asUnion() { return this; }
}
