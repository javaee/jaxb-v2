/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.ct;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;

import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.FALLBACK_CONTENT;
import static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode.NORMAL;

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
                CPropertyInfo p = prop.createValueProperty("Value",false,ct,use);
                selector.getCurrentBean().addProperty(p);
            }

            public void particle(XSParticle p) {
                // determine the binding of this complex type.

                builder.recordBindingMode(ct,
                    particleBinder.checkFallback(p)?FALLBACK_CONTENT:NORMAL);

                particleBinder.build(p);

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
