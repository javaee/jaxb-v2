/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.xjc.model;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.xsom.XSComponent;
import com.sun.istack.Nullable;

import org.xml.sax.Locator;

/**
 * {@link AttributePropertyInfo} for the compiler.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CAttributePropertyInfo extends CSingleTypePropertyInfo implements AttributePropertyInfo<NType,NClass> {

    private final QName attName;
    private final boolean isRequired;

    /**
     * @param type
     *      Represents the bound type of this attribute.
     * @param typeName
     *      XML Schema type name of this attribute. Optional for other schema languages.
     */
    public CAttributePropertyInfo(String name, XSComponent source, CCustomizations customizations,
                                  Locator locator, QName attName, TypeUse type, @Nullable QName typeName,
                                  boolean required ) {
        super(name, type, typeName, source, customizations, locator);
        isRequired = required;
        this.attName = attName;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public QName getXmlName() {
        return attName;
    }

    /**
     * An optional attribute can never be unboxable,
     * for we need null to represent the absence.
     */
    public boolean isUnboxable() {
        if(!isRequired) return false;
        return super.isUnboxable();
    }

    @Override
    public boolean isOptionalPrimitive() {
        return !isRequired && super.isUnboxable();
    }

    public <V> V accept(CPropertyVisitor<V> visitor) {
        return visitor.onAttribute(this);
    }
    @Override
    public <R, P> R accept(CPropertyVisitor2<R, P> visitor, P p) {
        return visitor.visit(this, p);
    }

    public final PropertyKind kind() {
        return  PropertyKind.ATTRIBUTE;
    }
}
