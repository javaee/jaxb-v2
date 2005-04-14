/*
 * @(#)$Id: ListSimpleTypeImpl.java,v 1.1 2005-04-14 22:06:25 kohsuke Exp $
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
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;

public class ListSimpleTypeImpl extends SimpleTypeImpl implements XSListSimpleType
{
    public ListSimpleTypeImpl( SchemaImpl _parent,
        AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa,
        String _name, boolean _anonymous,
        Ref.SimpleType _itemType ) {
        
        super(_parent,_annon,_loc,_fa,_name,_anonymous,
            _parent.parent.anySimpleType);
        
        this.itemType = _itemType;
    }
        
    private final Ref.SimpleType itemType;
    public XSSimpleType getItemType() { return itemType.getSimpleType(); }
    
    public void visit( XSSimpleTypeVisitor visitor ) {
        visitor.listSimpleType(this);
    }
    public Object apply( XSSimpleTypeFunction function ) {
        return function.listSimpleType(this);
    }

    // list type by itself doesn't have any facet. */
    public XSFacet getFacet( String name ) { return null; }
    
    public XSVariety getVariety() { return XSVariety.LIST; }

    public boolean isList() { return true; }
    public XSListSimpleType asList() { return this; }
}
