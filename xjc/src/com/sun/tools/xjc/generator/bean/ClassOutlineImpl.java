/*
 * @(#)$Id: ClassOutlineImpl.java,v 1.1 2005-04-15 20:09:04 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.ClassOutline;

/**
 * {@link ClassOutline} enhanced with schema2java specific
 * information.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ClassOutlineImpl extends ClassOutline {
    private final BeanGenerator _parent;


    public MethodWriter createMethodWriter() {
        return _parent.getModel().strategy.createMethodWriter(this);
    }
    
    /**
     * Gets {@link #_package} as {@link PackageOutlineImpl},
     * since it's guaranteed to be of that type.
     */
    public PackageOutlineImpl _package() {
        return (PackageOutlineImpl)super._package();
    }

    ClassOutlineImpl( BeanGenerator _parent,
        CClassInfo _target, JDefinedClass exposedClass, JDefinedClass _implClass, JClass _implRef ) {
        super(_target,exposedClass,_implRef,_implClass);
        this._parent = _parent;
    }

    public BeanGenerator parent() {
        return _parent;
    }
}
