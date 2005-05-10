/*
 * @(#)$Id: PackageOutlineImpl.java,v 1.4 2005-05-10 23:12:33 kohsuke Exp $
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

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CPropertyVisitor;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CValuePropertyInfo;
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

    private String mostUsedNamespaceURI;
    private XmlNsForm elementFormDefault;

    /**
     * The namespace URI most commonly used in classes in this package.
     * This should be used as the namespace URI for {@link XmlSchema#namespace()}.
     *
     * <p>
     * Null if no default
     *
     * @see #calcDefaultValues().
     */
    public String getMostUsedNamespaceURI() {
        return mostUsedNamespaceURI;
    }

    /**
     * The element form default for this package.
     * <p>
     * The value is computed by examining what would yield the smallest generated code.
     */
    public XmlNsForm getElementFormDefault() {
        assert elementFormDefault!=null;
        return elementFormDefault;
    }

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

    /**
     * Compute the most common namespace URI in this package
     * (to put into {@link XmlSchema#namespace()} and what value
     * we should put into {@link XmlSchema#elementFormDefault()}.
     *
     * This method is called after {@link #classes} field is filled up.
     */
    public void calcDefaultValues() {
        // TODO for Ryan to compute those values properly

        // used to visit properties
        CPropertyVisitor<Void> propVisitor = new CPropertyVisitor<Void>() {
            public Void onElement(CElementPropertyInfo p) {
                // TODO
                return null;
            }

            public Void onReference(CReferencePropertyInfo p) {
                // TODO
                return null;
            }

            public Void onAttribute(CAttributePropertyInfo p) {
                return null;
            }

            public Void onValue(CValuePropertyInfo p) {
                return null;
            }
        };


        for (ClassOutlineImpl co : classes) {
            CClassInfo ci = co.target;
            // things you can look at
            ci.getTypeName();
            ci.getElementName();
            for( CPropertyInfo p : ci.getProperties() )
                p.accept(propVisitor);
        }
        mostUsedNamespaceURI = null;
        elementFormDefault = XmlNsForm.QUALIFIED;
    }
}
