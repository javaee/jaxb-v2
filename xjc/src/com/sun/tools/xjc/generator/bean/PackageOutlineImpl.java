/*
 * @(#)$Id: PackageOutlineImpl.java,v 1.6 2005-05-17 23:20:42 ryan_shoemaker Exp $
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
import java.util.HashMap;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

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
        // if possible, we should also have a switch that tells XJC not to use a package-level
        // annotation, so that people can have self-contained classes.

        // used to visit properties
        CPropertyVisitor<Void> propVisitor = new CPropertyVisitor<Void>() {
            public Void onElement(CElementPropertyInfo p) {
                countURI(propUriCountMap, p.getXmlName());
                return null;
            }

            public Void onReference(CReferencePropertyInfo p) {
                countURI(propUriCountMap, p.getXmlName());
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
            countURI(uriCountMap, ci.getTypeName());
            countURI(uriCountMap, ci.getElementName());

            for( CPropertyInfo p : ci.getProperties() )
                p.accept(propVisitor);
        }
        mostUsedNamespaceURI = getMostUsedURI(uriCountMap);
        elementFormDefault = getFormDefault();

        // debug code
        // System.out.println(uriCountMap.size() + ": " + _package.name() + ": " + mostUsedNamespaceURI);
        // System.out.println(elementFormDefault);
    }

    // Map to keep track of how often each type or element uri is used in this package
    // mostly used to calculate mostUsedNamespaceURI
    private HashMap<String, Integer> uriCountMap = new HashMap<String, Integer>();

    // Map to keep track of how often each property uri is used in this package
    // used to calculate elementFormDefault
    private HashMap<String, Integer> propUriCountMap = new HashMap<String, Integer>();

    /**
     * pull the uri out of the specified QName and keep track of it in the
     * specified hash map
     *
     * @param qname
     */
    private void countURI(HashMap<String, Integer> map, QName qname) {
        if (qname == null) return;

        String uri = qname.getNamespaceURI();

        if (map.containsKey(uri)) {
            map.put(uri, map.get(uri).intValue() + 1);
        } else {
            map.put(uri, 1);
        }
    }

    /**
     * Iterate through the hash map looking for the namespace used
     * most frequently.  Ties are arbitrarily broken by the order
     * in which the map keys are iterated over.
     */
    private String getMostUsedURI(HashMap<String, Integer> map) {
        String mostPopular = null;
        int count = 0;

        for (String uri : map.keySet()) {
            int uriCount = map.get(uri);
            if (mostPopular == null) {
                mostPopular = uri;
                count = uriCount;
            } else {
                if (uriCount > count) {
                    mostPopular = uri;
                    count = uriCount;
                }
            }
        }

        if (mostPopular == null) return "";
        return mostPopular;
    }

    /**
     * Calculate the element form defaulting.
     *
     * Compare the most frequently used property URI to the most frequently used
     * element/type URI.  If they match, then return QUALIFIED
     * @return
     */
    private XmlNsForm getFormDefault() {
        if (getMostUsedURI(propUriCountMap).equals("")) return XmlNsForm.UNQUALIFIED;
        else return XmlNsForm.QUALIFIED;
    }
}
