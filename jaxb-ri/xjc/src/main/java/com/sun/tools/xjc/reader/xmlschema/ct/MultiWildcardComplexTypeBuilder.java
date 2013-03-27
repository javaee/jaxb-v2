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

package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.RawTypeSet;
import com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.FALLBACK_CONTENT;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSType;

/**
 * @author Kohsuke Kawaguchi
 */
final class MultiWildcardComplexTypeBuilder extends CTBuilder {

    public boolean isApplicable(XSComplexType ct) {
        if (!bgmBuilder.model.options.contentForWildcard) {
            return false;
        }
        XSType bt = ct.getBaseType();
        if (bt ==schemas.getAnyType() && ct.getContentType() != null) {
            XSParticle part = ct.getContentType().asParticle();
            if ((part != null) && (part.getTerm().isModelGroup())) {
                XSParticle[] parts = part.getTerm().asModelGroup().getChildren();
                int wildcardCount = 0;
                int i = 0;
                while ((i < parts.length) && (wildcardCount <= 1)) {
                    if (parts[i].getTerm().isWildcard()) {
                        wildcardCount += 1;
                    }
                    i++;
                }
                return (wildcardCount > 1);
            }
        }
        return false;
    }

    public void build(XSComplexType ct) {
        XSContentType contentType = ct.getContentType();

        builder.recordBindingMode(ct, FALLBACK_CONTENT);
        BIProperty prop = BIProperty.getCustomization(ct);

        CPropertyInfo p;

        if(contentType.asEmpty()!=null) {
            p = prop.createValueProperty("Content",false,ct,CBuiltinLeafInfo.STRING,null);
        } else {
            RawTypeSet ts = RawTypeSetBuilder.build(contentType.asParticle(),false);
            p = prop.createReferenceProperty("Content", false, ct, ts, true, false, true, false);
        }

        selector.getCurrentBean().addProperty(p);

        // adds attributes and we are through.
        green.attContainer(ct);
    }

}
