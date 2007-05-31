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
package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.FALLBACK_CONTENT;
import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.NORMAL;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;

/**
 * Builds a complex type that inherits from the anyType complex type.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class FreshComplexTypeBuilder extends CTBuilder {

    public boolean isApplicable(XSComplexType ct) {
        return ct.getBaseType()==schemas.getAnyType()
            &&  !ct.isMixed();  // not mixed
    }

    public void build(final XSComplexType ct) {
        XSContentType contentType = ct.getContentType();

        contentType.visit(new XSContentTypeVisitor() {
            public void simpleType(XSSimpleType st) {
                builder.recordBindingMode(ct,ComplexTypeBindingMode.NORMAL);

                simpleTypeBuilder.refererStack.push(ct);
                TypeUse use = simpleTypeBuilder.build(st);
                simpleTypeBuilder.refererStack.pop();

                BIProperty prop = BIProperty.getCustomization(ct);
                CPropertyInfo p = prop.createValueProperty("Value",false,ct,use, BGMBuilder.getName(st));
                selector.getCurrentBean().addProperty(p);
            }

            public void particle(XSParticle p) {
                // determine the binding of this complex type.

                builder.recordBindingMode(ct,
                    bgmBuilder.getParticleBinder().checkFallback(p)?FALLBACK_CONTENT:NORMAL);

                bgmBuilder.getParticleBinder().build(p);

                XSTerm term = p.getTerm();
                if(term.isModelGroup() && term.asModelGroup().getCompositor()==XSModelGroup.ALL)
                    selector.getCurrentBean().setOrdered(false);

            }

            public void empty(XSContentType e) {
                builder.recordBindingMode(ct,NORMAL);
            }
        });

        // adds attributes and we are through.
        green.attContainer(ct);
    }

}
