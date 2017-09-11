/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.bind.v2.model.impl;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * Common implementation between {@link ClassInfoImpl} and {@link ElementInfoImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class TypeInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
        implements TypeInfo<TypeT,ClassDeclT>, Locatable {

    /**
     * The Java class that caused this Java class to be a part of the JAXB processing.
     *
     * null if it's specified explicitly by the user.
     */
    private final Locatable upstream;

    /**
     * {@link TypeInfoSet} to which this class belongs.
     */
    protected final TypeInfoSetImpl<TypeT,ClassDeclT,FieldT,MethodT> owner;

    /**
     * Reference to the {@link ModelBuilder}, only until we link {@link TypeInfo}s all together,
     * because we don't want to keep {@link ModelBuilder} too long.
     */
    protected ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> builder;

    protected TypeInfoImpl(
        ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> builder,
        Locatable upstream) {

        this.builder = builder;
        this.owner = builder.typeInfoSet;
        this.upstream = upstream;
    }

    public Locatable getUpstream() {
        return upstream;
    }

    /*package*/ void link() {
        builder = null;
    }

    protected final Navigator<TypeT,ClassDeclT,FieldT,MethodT> nav() {
        return owner.nav;
    }

    protected final AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> reader() {
        return owner.reader;
    }

    /**
     * Parses an {@link XmlRootElement} annotation on a class
     * and determine the element name.
     *
     * @return null
     *      if none was found.
     */
    protected final QName parseElementName(ClassDeclT clazz) {
        XmlRootElement e = reader().getClassAnnotation(XmlRootElement.class,clazz,this);
        if(e==null)
            return null;

        String local = e.name();
        if(local.equals("##default")) {
            // if defaulted...
            local = NameConverter.standard.toVariableName(nav().getClassShortName(clazz));
        }
        String nsUri = e.namespace();
        if(nsUri.equals("##default")) {
            // if defaulted ...
            XmlSchema xs = reader().getPackageAnnotation(XmlSchema.class,clazz,this);
            if(xs!=null)
                nsUri = xs.namespace();
            else {
                nsUri = builder.defaultNsUri;
            }
        }

        return new QName(nsUri.intern(),local.intern());
    }

    protected final QName parseTypeName(ClassDeclT clazz) {
        return parseTypeName( clazz, reader().getClassAnnotation(XmlType.class,clazz,this) );
    }

    /**
     * Parses a (potentially-null) {@link XmlType} annotation on a class
     * and determine the actual value.
     *
     * @param clazz
     *      The class on which the XmlType annotation is checked.
     * @param t
     *      The {@link XmlType} annotation on the clazz. This value
     *      is taken as a parameter to improve the performance for the case where
     *      't' is pre-computed.
     */
    protected final QName parseTypeName(ClassDeclT clazz, XmlType t) {
        String nsUri="##default";
        String local="##default";
        if(t!=null) {
            nsUri = t.namespace();
            local = t.name();
        }

        if(local.length()==0)
            return null; // anonymous


        if(local.equals("##default"))
            // if defaulted ...
            local = NameConverter.standard.toVariableName(nav().getClassShortName(clazz));

        if(nsUri.equals("##default")) {
            // if defaulted ...
            XmlSchema xs = reader().getPackageAnnotation(XmlSchema.class,clazz,this);
            if(xs!=null)
                nsUri = xs.namespace();
            else {
                nsUri = builder.defaultNsUri;
            }
        }

        return new QName(nsUri.intern(),local.intern());
    }
}
