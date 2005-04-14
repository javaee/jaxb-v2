/*
 * @(#)$Id: DeclarationImpl.java,v 1.1 2005-04-14 22:06:25 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.util.NameGetter;

abstract class DeclarationImpl extends ComponentImpl implements XSDeclaration
{
    DeclarationImpl( SchemaImpl owner,
        AnnotationImpl _annon, Locator loc, ForeignAttributesImpl fa,
        String _targetNamespace, String _name,    boolean _anonymous ) {
        
        super(owner,_annon,loc,fa);
        this.targetNamespace = _targetNamespace;
        this.name = _name;
        this.anonymous = _anonymous;
    }
    
    private final String name;
    public String getName() { return name; }
    
    private final String targetNamespace;
    public String getTargetNamespace() { return targetNamespace; }
    
    private final boolean anonymous;
    /** @deprecated */
    public boolean isAnonymous() { return anonymous; }
    
    public final boolean isGlobal() { return !isAnonymous(); }
    public final boolean isLocal() { return isAnonymous(); }
    
    
    public String toString() {
        return NameGetter.get(this)+" "+getName();
    }
}
