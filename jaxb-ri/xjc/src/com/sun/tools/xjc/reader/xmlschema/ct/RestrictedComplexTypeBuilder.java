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

import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSType;

/**
 * Binds a complex type derived from another complex type
 * by restriction.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class RestrictedComplexTypeBuilder extends CTBuilder {

    public boolean isApplicable(XSComplexType ct) {
        XSType baseType = ct.getBaseType();
        return baseType!=schemas.getAnyType()
            &&  baseType.isComplexType()
            &&  ct.getDerivationMethod()==XSType.RESTRICTION;
    }

    public void build(XSComplexType ct) {

        if (bgmBuilder.getGlobalBinding().isRestrictionFreshType()) {
            // handle derivation-by-restriction like a whole new type
            new FreshComplexTypeBuilder().build(ct);
            return;
        }

        XSComplexType baseType = ct.getBaseType().asComplexType();

        // build the base class
        CClass baseClass = selector.bindToType(baseType,ct,true);
        assert baseClass!=null;   // global complex type must map to a class
        
        selector.getCurrentBean().setBaseClass(baseClass);
        
        if (bgmBuilder.isGenerateMixedExtensions()) {
            boolean forceFallbackInExtension = baseType.isMixed() &&
                                               ct.isMixed() &&
                                               (ct.getExplicitContent() != null) &&
                                               bgmBuilder.inExtensionMode;
            if (forceFallbackInExtension) {
                builder.recordBindingMode(ct, ComplexTypeBindingMode.NORMAL);

                BIProperty prop = BIProperty.getCustomization(ct);
                CPropertyInfo p;

                XSParticle particle = ct.getContentType().asParticle();
                if (particle != null) {
                    RawTypeSet ts = RawTypeSetBuilder.build(particle, false);
                    p = prop.createExtendedMixedReferenceProperty("Content", ct, ts);
                    selector.getCurrentBean().addProperty(p);
                }
            } else {
                // determine the binding of this complex type.
                builder.recordBindingMode(ct,builder.getBindingMode(baseType));
            }
        } else {
            builder.recordBindingMode(ct,builder.getBindingMode(baseType));
        }
    }
}
