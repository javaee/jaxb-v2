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

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;

/**
 * Common part of {@link ElementPropertyInfoImpl} and {@link ReferencePropertyInfoImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ERPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> {

    public ERPropertyInfoImpl(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> classInfo, PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> propertySeed) {
        super(classInfo, propertySeed);

        XmlElementWrapper e = seed.readAnnotation(XmlElementWrapper.class);

        boolean nil = false;
        boolean required = false;
        if(!isCollection()) {
            xmlName = null;
            if(e!=null)
                classInfo.builder.reportError(new IllegalAnnotationException(
                    Messages.XML_ELEMENT_WRAPPER_ON_NON_COLLECTION.format(
                        nav().getClassName(parent.getClazz())+'.'+seed.getName()),
                    e
                ));
        } else {
            if(e!=null) {
                xmlName = calcXmlName(e);
                nil = e.nillable();
                required = e.required();
            } else
                xmlName = null;
        }

        wrapperNillable = nil;
        wrapperRequired = required;
    }

    private final QName xmlName;

    /**
     * True if the wrapper tag name is nillable.
     */
    private final boolean wrapperNillable;

    /**
     * True if the wrapper tag is required.
     */
    private final boolean wrapperRequired;

    /**
     * Gets the wrapper element name.
     */
    public final QName getXmlName() {
        return xmlName;
    }

    public final boolean isCollectionNillable() {
        return wrapperNillable;
    }

    public final boolean isCollectionRequired() {
        return wrapperRequired;
    }
}
