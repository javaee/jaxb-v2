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

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class ColorBinder extends BindingComponent implements XSVisitor {
    protected final BGMBuilder builder = Ring.get(BGMBuilder.class);
    protected final ClassSelector selector = getClassSelector();

    protected final CClassInfo getCurrentBean() {
        return selector.getCurrentBean();
    }
    protected final XSComponent getCurrentRoot() {
        return selector.getCurrentRoot();
    }


    protected final void createSimpleTypeProperty(XSSimpleType type,String propName) {
        BIProperty prop = BIProperty.getCustomization(type);

        SimpleTypeBuilder stb = Ring.get(SimpleTypeBuilder.class);
        // since we are building the simple type here, use buildDef
        CPropertyInfo p = prop.createValueProperty(propName,false,type,stb.buildDef(type),BGMBuilder.getName(type));
        getCurrentBean().addProperty(p);
    }





    public final void annotation(XSAnnotation xsAnnotation) {
        throw new IllegalStateException();
    }

    public final void schema(XSSchema xsSchema) {
        throw new IllegalStateException();
    }

    public final void facet(XSFacet xsFacet) {
        throw new IllegalStateException();
    }

    public final void notation(XSNotation xsNotation) {
        throw new IllegalStateException();
    }

    public final void identityConstraint(XSIdentityConstraint xsIdentityConstraint) {
        throw new IllegalStateException();
    }

    public final void xpath(XSXPath xsxPath) {
        throw new IllegalStateException();
    }
}
