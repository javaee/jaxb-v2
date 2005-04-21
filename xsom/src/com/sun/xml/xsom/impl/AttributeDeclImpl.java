/*
 * @(#)$Id: AttributeDeclImpl.java,v 1.2 2005-04-21 16:42:12 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public class AttributeDeclImpl extends DeclarationImpl implements XSAttributeDecl, Ref.Attribute
{
    public AttributeDeclImpl( SchemaImpl owner,
        String _targetNamespace, String _name,
        AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, boolean _anonymous,
        String _defValue, String _fixedValue, ValidationContext _context,
        Ref.SimpleType _type ) {
        
        super(owner,_annon,_loc,_fa,_targetNamespace,_name,_anonymous);
        
        if(_name==null) // assertion failed.
            throw new IllegalArgumentException();
        
        this.defaultValue = _defValue;
        this.fixedValue = _fixedValue;
        this.context = _context;    
        this.type = _type;
    }
    
    private final Ref.SimpleType type;
    public XSSimpleType getType() { return type.getType(); }

    private final ValidationContext context;
    public ValidationContext getContext() { return context; }
    
    private final String defaultValue;
    public String getDefaultValue() { return defaultValue; }
    
    private final String fixedValue;
    public String getFixedValue() { return fixedValue; }
    
    public void visit( XSVisitor visitor ) {
        visitor.attributeDecl(this);
    }
    public Object apply( XSFunction function ) {
        return function.attributeDecl(this);
    }


    // Ref.Attribute implementation
    public XSAttributeDecl getAttribute() { return this; }
 }
