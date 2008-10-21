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
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSType;

/**
 * @author Kohsuke Kawaguchi
 */
final class MixedExtendedComplexTypeBuilder extends AbstractExtendedComplexTypeBuilder {

    public boolean isApplicable(XSComplexType ct) {
        XSType bt = ct.getBaseType();
        if (bt.isComplexType() &&
            bt.asComplexType().isMixed() &&
            ct.isMixed() &&
            ct.getDerivationMethod()==XSType.EXTENSION &&
            ct.getContentType().asParticle() != null &&
            ct.getExplicitContent().asEmpty() == null
            )  {
                return true;
        }

        return false;
    }

    private boolean checkFallback(XSComplexType t) {
//        (bt.getBaseType() != schemas.getAnyType())
//                            ||
//                (bt.asComplexType().getExplicitContent() != null &&
//                 bt.asComplexType().getExplicitContent().asParticle() != null) &&
//                 (bgmBuilder.getParticleBinder().checkFallback(bt.asComplexType().getExplicitContent().asParticle()) && // continues (can't check for CONTENT binding mode itself because the element might not have been bound yet

        return false;
    }

    public void build(XSComplexType ct) {
        System.out.println("-----\nMixed Extended: " + ct.getName());

        XSComplexType baseType = ct.getBaseType().asComplexType();
        System.out.println("Base Type: " + baseType.getName());

        // build the base class
        CClass baseClass = selector.bindToType(baseType, ct, true);
        assert baseClass != null;   // global complex type must map to a class

        if (!checkIfExtensionSafe(baseType, ct)) {
            // error. We can't handle any further extension
            errorReceiver.error(ct.getLocator(),
                    Messages.ERR_NO_FURTHER_EXTENSION.format(
                    baseType.getName(), ct.getName() )
            );
            return;
        }

        selector.getCurrentBean().setBaseClass(baseClass);
        builder.recordBindingMode(ct, ComplexTypeBindingMode.FALLBACK_EXTENSION);

        BIProperty prop = BIProperty.getCustomization(ct);
        CPropertyInfo p;

        RawTypeSet ts = RawTypeSetBuilder.build(ct.getContentType().asParticle(), false);
        p = prop.createExtendedMixedReferenceProperty("contentOverrideFor" + ct.getName(), ct, ts);

        System.out.println("Children: " + prop.getChildren());
        
        selector.getCurrentBean().addProperty(p);

        // adds attributes and we are through.
        green.attContainer(ct);        
    }

}
