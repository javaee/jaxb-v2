/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import java.util.Collections;
import java.util.List;

import javax.activation.MimeType;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * {@link CPropertyInfo} backed by a single {@link TypeUse}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class CSingleTypePropertyInfo extends CPropertyInfo {
    protected final TypeUse type;

    private final QName schemaType;

    /**
     *
     * @param typeName
     *      XML Schema type name of this property's single value. Optional
     *      for other schema languages. This is used to determine if we should
     *      generate {@link @XmlSchemaType} annotation to improve the roundtrip.
     */
    protected CSingleTypePropertyInfo(String name, TypeUse type, QName typeName, XSComponent source, CCustomizations customizations, Locator locator) {
        super(name, type.isCollection(), source, customizations, locator);
        this.type = type;

        if(needsExplicitTypeName(type,typeName))
            schemaType = typeName;
        else
            schemaType = null;
    }

    public QName getSchemaType() {
        return schemaType;
    }

    public final ID id() {
        return type.idUse();
    }

    public final MimeType getExpectedMimeType() {
        return type.getExpectedMimeType();
    }

    public final List<? extends CTypeInfo> ref() {
        return Collections.singletonList(getTarget());
    }

    public final CNonElement getTarget() {
        CNonElement r = type.getInfo();
        assert r!=null;
        return r;
    }

    public final CAdapter getAdapter() {
        return type.getAdapterUse();
    }

    public final CSingleTypePropertyInfo getSource() {
        return this;
    }
}
