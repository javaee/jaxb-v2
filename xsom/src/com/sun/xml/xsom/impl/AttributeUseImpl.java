/*
 * @(#)$Id: AttributeUseImpl.java,v 1.1 2005-04-14 22:06:24 kohsuke Exp $
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
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public class AttributeUseImpl extends ComponentImpl implements XSAttributeUse
{
    public AttributeUseImpl( SchemaImpl owner, AnnotationImpl ann, Locator loc, ForeignAttributesImpl fa, Ref.Attribute _decl,
        String def, String fixed, ValidationContext _context, boolean req ) {
        
        super(owner,ann,loc,fa);
        
        this.att = _decl;
        this.defaultValue = def;
        this.fixedValue = fixed;
        this.context = _context;
        this.required = req;
    }
    
    private final Ref.Attribute att;    
    public XSAttributeDecl getDecl() { return att.getAttribute(); }
    
    private final String defaultValue;
    public String getDefaultValue() {
        if( defaultValue!=null )    return defaultValue;
        else                        return getDecl().getDefaultValue();
    }
    
    private final String fixedValue;
    public String getFixedValue() {
        if( fixedValue!=null )      return fixedValue;
        else                        return getDecl().getFixedValue();
    }
    
    private final ValidationContext context;
    public ValidationContext getContext() {
        if( fixedValue!=null || defaultValue!=null )    return context;
        else    return getDecl().getContext();
    }
    
    private final boolean required;
    public boolean isRequired() { return required; }
    
    public Object apply( XSFunction f ) {
        return f.attributeUse(this);
    }
    public void visit( XSVisitor v ) {
        v.attributeUse(this);
    }
}
