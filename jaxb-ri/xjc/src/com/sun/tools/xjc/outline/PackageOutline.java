/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.outline;

import java.util.Set;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator;

/**
 * Outline object that provides per-package information.
 * 
 * This interface is accessible from {@link Outline}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface PackageOutline {
    
    /**
     * The exposed package this context is representing.
     *
     * <p>
     * An exposed package is a package visible to users, a package
     * supposed to be used by client applications. Sometime
     * we have another parallel package that's not visible to users.
     */
    JPackage _package();

    /**
     * Generated ObjectFactory from package.
     * 
     * This method allows a caller to obtain a reference to such
     * ObjectFactory from its package.
     *
     * Must not be null.
     */
    JDefinedClass objectFactory();

    /**
     * Generates an ObjectFactory class for this package.
     */
    ObjectFactoryGenerator objectFactoryGenerator();

    /**
     * Gets {@link ClassOutline}s whose {@link ClassOutline#_package()}
     * points to this object.
     *
     * @return can be empty but never null.
     */
    Set<? extends ClassOutline> getClasses();

    /**
     * The namespace URI most commonly used in classes in this package.
     * This should be used as the namespace URI for {@link XmlSchema#namespace()}.
     *
     * <p>
     * Null if no default
     */
    public String getMostUsedNamespaceURI();

    /**
     * The element form default for this package.
     * <p>
     * The value is computed by examining what would yield the smallest generated code.
     */
    public XmlNsForm getElementFormDefault();

}
