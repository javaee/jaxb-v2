/*
 * @(#)$Id: ElementDecl.java,v 1.1 2005-04-14 22:06:25 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.impl.parser.PatcherManager;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import com.sun.xml.xsom.visitor.XSTermFunctionWithParam;
import org.xml.sax.Locator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class ElementDecl extends DeclarationImpl implements XSElementDecl, Ref.Term
{
    public ElementDecl( PatcherManager reader, SchemaImpl owner,
        AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl fa,
        String _tns, String _name, boolean _anonymous,
        
        String _defv, String _fixedv, boolean _nillable, boolean _abstract,
        Ref.Type _type, Ref.Element _substHead,
        int _substDisallowed, int _substExcluded,
        List<IdentityConstraintImpl> idConstraints ) {
        
        super(owner,_annon,_loc,fa,_tns,_name,_anonymous);
        
        this.defaultValue = _defv;
        this.fixedValue = _fixedv;
        this.nillable = _nillable;
        this._abstract = _abstract;
        this.type = _type;
        this.substHead = _substHead;
        this.substDisallowed = _substDisallowed;
        this.substExcluded = _substExcluded;
        this.idConstraints = Collections.unmodifiableList((List<? extends XSIdentityConstraint>)idConstraints);

        for (IdentityConstraintImpl idc : idConstraints)
            idc.setParent(this);

        if(type==null)
            throw new IllegalArgumentException();
    }
    
    private String defaultValue;
    public String getDefaultValue() { return defaultValue; }
    
    private String fixedValue;
    public String getFixedValue() { return fixedValue; }
    
    private boolean nillable;
    public boolean isNillable() { return nillable; }
    
    private boolean _abstract;
    public boolean isAbstract() { return _abstract; }
    
    private Ref.Type type;
    public XSType getType() { return type.getType(); }
    
    private Ref.Element substHead;
    public XSElementDecl getSubstAffiliation() {
        if(substHead==null)     return null;
        return substHead.get();
    }
    
    private int substDisallowed;
    public boolean isSubstitutionDisallowed( int method ) {
        return (substDisallowed&method)!=0;
    }
    
    private int substExcluded;
    public boolean isSubstitutionExcluded( int method ) {
        return (substExcluded&method)!=0;
    }

    private final List<XSIdentityConstraint> idConstraints;
    public List<XSIdentityConstraint> getIdentityConstraints() {
        return idConstraints;
    }


    /**
     * @deprecated 
     */
    public XSElementDecl[] listSubstitutables() {
        Set<? extends XSElementDecl> s = getSubstitutables();
        return s.toArray(new XSElementDecl[s.size()]);
    }

    /** Set that represents element decls that can substitute this element. */
    private Set<XSElementDecl> substitutables = null;

    /** Unmodifieable view of {@link #substitutables}. */
    private Set<XSElementDecl> substitutablesView = null;
    
    public Set<? extends XSElementDecl> getSubstitutables() {
        if( substitutables==null ) {
            // if the field is null by the time this method
            // is called, it means this element is substitutable by itself only.
            substitutables = substitutablesView = Collections.singleton((XSElementDecl)this);
        }
        return substitutablesView;
    }
    
    protected void addSubstitutable( ElementDecl decl ) {
        if( substitutables==null ) {
            substitutables = new HashSet<XSElementDecl>();
            substitutables.add(this);
            substitutablesView = Collections.unmodifiableSet(substitutables);
        }
        substitutables.add(decl);
    }
    
    
    public void updateSubstitutabilityMap() {
        ElementDecl parent = this;
        XSType type = this.getType(); 

        boolean rused = false;
        boolean eused = false;
        
        while( (parent=(ElementDecl)parent.getSubstAffiliation())!=null ) {
            
            if(parent.isSubstitutionDisallowed(XSType.SUBSTITUTION))
                continue;
            
            boolean rd = parent.isSubstitutionDisallowed(XSType.RESTRICTION);
            boolean ed = parent.isSubstitutionDisallowed(XSType.EXTENSION);

            if( (rd && rused) || ( ed && eused ) )   continue;
            
            XSType parentType = parent.getType();
            while(type!=parentType) {
                if(type.getDerivationMethod()==XSType.RESTRICTION)  rused = true;
                else                                                eused = true;
                
                type = type.getBaseType();
                if(type==null)  // parentType and type doesn't share the common base type. a bug in the schema.
                    break;
                
                if( type.isComplexType() ) {
                    rd |= type.asComplexType().isSubstitutionProhibited(XSType.RESTRICTION);
                    ed |= type.asComplexType().isSubstitutionProhibited(XSType.EXTENSION);
                }
            }
            
            if( (rd && rused) || ( ed && eused ) )   continue;
            
            // this element can substitute "parent"
            parent.addSubstitutable(this);
        }
    }
    
    public boolean canBeSubstitutedBy(XSElementDecl e) {
        return getSubstitutables().contains(e);
    }

    public boolean isWildcard()                 { return false; }
    public boolean isModelGroupDecl()           { return false; }
    public boolean isModelGroup()               { return false; }
    public boolean isElementDecl()              { return true; }

    public XSWildcard asWildcard()              { return null; }
    public XSModelGroupDecl asModelGroupDecl()  { return null; }
    public XSModelGroup asModelGroup()          { return null; }
    public XSElementDecl asElementDecl()        { return this; }



    
    public void visit( XSVisitor visitor ) {
        visitor.elementDecl(this);
    }
    public void visit( XSTermVisitor visitor ) {
        visitor.elementDecl(this);
    }
    public Object apply( XSTermFunction function ) {
        return function.elementDecl(this);
    }

    public <T,P> T apply(XSTermFunctionWithParam<T, P> function, P param) {
        return function.elementDecl(this,param);
    }

    public Object apply( XSFunction function ) {
        return function.elementDecl(this);
    }
    
    
    // Ref.Term implementation
    public XSTerm getTerm() { return this; }
}
