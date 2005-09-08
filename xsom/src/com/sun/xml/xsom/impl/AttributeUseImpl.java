/*
 * @(#)$Id: AttributeUseImpl.java,v 1.2 2005-09-08 22:49:29 kohsuke Exp $
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
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public class AttributeUseImpl extends ComponentImpl implements XSAttributeUse
{
    public AttributeUseImpl( SchemaImpl owner, AnnotationImpl ann, Locator loc, ForeignAttributesImpl fa, Ref.Attribute _decl,
        XmlString def, XmlString fixed, boolean req ) {
        
        super(owner,ann,loc,fa);
        
        this.att = _decl;
        this.defaultValue = def;
        this.fixedValue = fixed;
        this.required = req;
    }
    
    private final Ref.Attribute att;    
    public XSAttributeDecl getDecl() { return att.getAttribute(); }
    
    private final XmlString defaultValue;
    public XmlString getDefaultValue() {
        if( defaultValue!=null )    return defaultValue;
        else                        return getDecl().getDefaultValue();
    }
    
    private final XmlString fixedValue;
    public XmlString getFixedValue() {
        if( fixedValue!=null )      return fixedValue;
        else                        return getDecl().getFixedValue();
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
