/*
 * @(#)$Id: PackageOutlineImpl.java,v 1.2 2005-05-10 22:51:08 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.PackageOutline;

/**
 * {@link PackageOutline} enhanced with schema2java specific
 * information.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class PackageOutlineImpl implements PackageOutline {
    private final JPackage _package;
    private final ObjectFactoryGenerator objectFactoryGenerator;

    /*package*/ final Set<ClassOutlineImpl> classes = new HashSet<ClassOutlineImpl>();
    private final Set<ClassOutlineImpl> classesView = Collections.unmodifiableSet(classes);

    public JPackage _package() {
        return _package;
    }

    public ObjectFactoryGenerator objectFactoryGenerator() {
        return objectFactoryGenerator;
    }

    public Set<ClassOutlineImpl> getClasses() {
        return classesView;
    }

    public JDefinedClass objectFactory() {
        return objectFactoryGenerator.getObjectFactory();
    }

    protected PackageOutlineImpl( BeanGenerator outline, Model model, JPackage _pkg ) {
        this._package = _pkg;
        switch(model.strategy) {
        case BEAN_ONLY:
            objectFactoryGenerator = new PublicObjectFactoryGenerator(outline,model,_pkg);
            break;
        case INTF_AND_IMPL:
            objectFactoryGenerator = new DualObjectFactoryGenerator(outline,model,_pkg);
            break;
        default:
            throw new IllegalStateException();
        }
    }
}
