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

package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CDefaultValue;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.CClass;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;

/**
 * @author Kohsuke Kawaguchi
 */
public class BindPurple extends ColorBinder {
    public void attGroupDecl(XSAttGroupDecl xsAttGroupDecl) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void attributeDecl(XSAttributeDecl xsAttributeDecl) {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * Attribute use always becomes a property.
     */
    public void attributeUse(XSAttributeUse use) {
        boolean hasFixedValue = use.getFixedValue()!=null;
        BIProperty pc = BIProperty.getCustomization(use);

        // map to a constant property ?
        boolean toConstant = pc.isConstantProperty() && hasFixedValue;
        TypeUse attType = bindAttDecl(use.getDecl());

        CPropertyInfo prop = pc.createAttributeProperty( use, attType );

        if(toConstant) {
            prop.defaultValue = CDefaultValue.create(attType,use.getFixedValue());
            prop.realization = builder.fieldRendererFactory.getConst(prop.realization);
        } else
        if(!attType.isCollection()) {
            // don't support a collection default value. That's difficult to do.

            if(use.getDefaultValue()!=null) {
                // this attribute use has a default value.
                // the item type is guaranteed to be a leaf type... or TODO: is it really so?
                // don't support default values if it's a list
                prop.defaultValue = CDefaultValue.create(attType,use.getDefaultValue());
            } else
            if(use.getFixedValue()!=null) {
                prop.defaultValue = CDefaultValue.create(attType,use.getFixedValue());
            }
        }

        getCurrentBean().addProperty(prop);
    }

    private TypeUse bindAttDecl(XSAttributeDecl decl) {
        SimpleTypeBuilder stb = Ring.get(SimpleTypeBuilder.class);
        stb.refererStack.push( decl );
        try {
            return stb.build(decl.getType());
        } finally {
            stb.refererStack.pop();
        }
    }


    public void complexType(XSComplexType ct) {
        CClass ctBean = selector.bindToType(ct,null,false);
        if(getCurrentBean()!=ctBean)
            // in some case complex type and element binds to the same class
            // don't make it has-a. Just make it is-a.
            getCurrentBean().setBaseClass(ctBean);
    }

    public void wildcard(XSWildcard xsWildcard) {
        // element wildcards are processed by particle binders,
        // so this one is for attribute wildcard.

        getCurrentBean().hasAttributeWildcard(true);
    }

    public void modelGroupDecl(XSModelGroupDecl xsModelGroupDecl) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void modelGroup(XSModelGroup xsModelGroup) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void elementDecl(XSElementDecl xsElementDecl) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void simpleType(XSSimpleType type) {
        createSimpleTypeProperty(type,"Value");
    }

    public void particle(XSParticle xsParticle) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void empty(XSContentType ct) {
        // empty generates nothing
    }
}
