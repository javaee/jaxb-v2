/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CElement;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;

/**
 * {@link ClassBinder} that delegates the call to another {@link ClassBinder}.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
abstract class ClassBinderFilter implements ClassBinder {
    private final ClassBinder core;

    protected ClassBinderFilter(ClassBinder core) {
        this.core = core;
    }

    public CElement annotation(XSAnnotation xsAnnotation) {
        return core.annotation(xsAnnotation);
    }

    public CElement attGroupDecl(XSAttGroupDecl xsAttGroupDecl) {
        return core.attGroupDecl(xsAttGroupDecl);
    }

    public CElement attributeDecl(XSAttributeDecl xsAttributeDecl) {
        return core.attributeDecl(xsAttributeDecl);
    }

    public CElement attributeUse(XSAttributeUse xsAttributeUse) {
        return core.attributeUse(xsAttributeUse);
    }

    public CElement complexType(XSComplexType xsComplexType) {
        return core.complexType(xsComplexType);
    }

    public CElement schema(XSSchema xsSchema) {
        return core.schema(xsSchema);
    }

    public CElement facet(XSFacet xsFacet) {
        return core.facet(xsFacet);
    }

    public CElement notation(XSNotation xsNotation) {
        return core.notation(xsNotation);
    }

    public CElement simpleType(XSSimpleType xsSimpleType) {
        return core.simpleType(xsSimpleType);
    }

    public CElement particle(XSParticle xsParticle) {
        return core.particle(xsParticle);
    }

    public CElement empty(XSContentType xsContentType) {
        return core.empty(xsContentType);
    }

    public CElement wildcard(XSWildcard xsWildcard) {
        return core.wildcard(xsWildcard);
    }

    public CElement modelGroupDecl(XSModelGroupDecl xsModelGroupDecl) {
        return core.modelGroupDecl(xsModelGroupDecl);
    }

    public CElement modelGroup(XSModelGroup xsModelGroup) {
        return core.modelGroup(xsModelGroup);
    }

    public CElement elementDecl(XSElementDecl xsElementDecl) {
        return core.elementDecl(xsElementDecl);
    }

    public CElement identityConstraint(XSIdentityConstraint xsIdentityConstraint) {
        return core.identityConstraint(xsIdentityConstraint);
    }

    public CElement xpath(XSXPath xsxPath) {
        return core.xpath(xsxPath);
    }
}
