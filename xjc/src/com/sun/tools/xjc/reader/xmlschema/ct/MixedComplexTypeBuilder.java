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

import com.sun.tools.javac.util.Options;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.FALLBACK_CONTENT;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSType;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
final class MixedComplexTypeBuilder extends CTBuilder {

    public boolean isApplicable(XSComplexType ct) {
        XSType bt = ct.getBaseType();
        if(bt ==schemas.getAnyType() && ct.isMixed())
            return true;    // fresh mixed complex type

        // there's no complex type in the inheritance tree yet
        if (bt.isComplexType() &&
            !bt.asComplexType().isMixed() &&
            ct.isMixed() &&
            ct.getDerivationMethod() == XSType.EXTENSION) {
                if (!bgmBuilder.isGenerateMixedExtensions() && (ct.getContentType().asParticle() == null)) {
                    return false;
                }
                return true;
        }

        return false;
    }

    public void build(XSComplexType ct) {
        XSContentType contentType = ct.getContentType();

        boolean generateMixedExtensions = bgmBuilder.isGenerateMixedExtensions();
        if (generateMixedExtensions) {
            if (!(ct.getBaseType() == schemas.getAnyType() && ct.isMixed())) {
                XSComplexType baseType = ct.getBaseType().asComplexType();
                // build the base class
                CClass baseClass = selector.bindToType(baseType, ct, true);
                selector.getCurrentBean().setBaseClass(baseClass);
            }
        }

        builder.recordBindingMode(ct, FALLBACK_CONTENT);
        BIProperty prop = BIProperty.getCustomization(ct);

        CPropertyInfo p;

        if (generateMixedExtensions) {            
            List<XSComplexType> cType = ct.getSubtypes();
            boolean isSubtyped = (cType != null) && (cType.size() > 0);

            if (contentType.asEmpty() != null) {
                if (isSubtyped) {
                    p = prop.createReferenceProperty("Content",false,ct, null, true, false, true);
                } else {
                    p = prop.createValueProperty("Content",false,ct,CBuiltinLeafInfo.STRING,null);
                }
            } else if (contentType.asParticle() == null) {
                p = prop.createReferenceProperty("Content",false,ct, null, true, false, true);
            } else {
                RawTypeSet ts = RawTypeSetBuilder.build(contentType.asParticle(), false);
                p = prop.createReferenceProperty("Content", false, ct, ts, true, false, true);
            }

        } else {
            if(contentType.asEmpty()!=null) {
                p = prop.createValueProperty("Content",false,ct,CBuiltinLeafInfo.STRING,null);
            } else {
                RawTypeSet ts = RawTypeSetBuilder.build(contentType.asParticle(),false);
                p = prop.createReferenceProperty("Content", false, ct, ts, true, false, true);
            }
        }

        selector.getCurrentBean().addProperty(p);

        // adds attributes and we are through.
        green.attContainer(ct);
    }

}
