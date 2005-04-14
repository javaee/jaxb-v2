/*
 * @(#)$Id: SimpleTypeImpl.java,v 1.1 2005-04-14 22:06:27 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public abstract class SimpleTypeImpl extends DeclarationImpl
    implements XSSimpleType, ContentTypeImpl, Ref.SimpleType
{
    SimpleTypeImpl(
        SchemaImpl _parent,
        AnnotationImpl _annon,
        Locator _loc,
        ForeignAttributesImpl _fa,
        String _name,
        boolean _anonymous,
        Ref.SimpleType _baseType) {

        super(_parent, _annon, _loc, _fa, _parent.getTargetNamespace(), _name, _anonymous);

        this.baseType = _baseType;
    }

    private Ref.SimpleType baseType;

    public XSType[] listSubstitutables() {
        return Util.listSubstitutables(this);
    }
    
    public void redefine( SimpleTypeImpl st ) {
        baseType = st;
    }
    
    public XSType getBaseType() { return baseType.getSimpleType(); }
    public XSSimpleType getSimpleBaseType() { return baseType.getSimpleType(); }

    
    public final int getDerivationMethod() { return XSType.RESTRICTION; }
    
    
    public final XSSimpleType asSimpleType()  { return this; }
    public final XSComplexType asComplexType(){ return null; }
    public final boolean isSimpleType()       { return true; }
    public final boolean isComplexType()      { return false; }
    public final XSParticle asParticle()      { return null; }
    public final XSContentType asEmpty()      { return null; }


    public boolean isRestriction() { return false; }
    public boolean isList() { return false; }
    public boolean isUnion() { return false; }
    public XSRestrictionSimpleType asRestriction() { return null; }
    public XSListSimpleType asList() { return null; }
    public XSUnionSimpleType asUnion() { return null; }
    
    
    

    public final void visit( XSVisitor visitor ) {
        visitor.simpleType(this);
    }
    public final void visit( XSContentTypeVisitor visitor ) {
        visitor.simpleType(this);
    }
    public final Object apply( XSFunction function ) {
        return function.simpleType(this);
    }
    public final Object apply( XSContentTypeFunction function ) {
        return function.simpleType(this);
    }
    
    // Ref.Type implementation
    public XSType getType() { return this; }
    // Ref.ContentType implementation
    public XSContentType getContentType() { return this; }
    // Ref.SimpleType implementation
    public XSSimpleType getSimpleType() { return this; }
}
