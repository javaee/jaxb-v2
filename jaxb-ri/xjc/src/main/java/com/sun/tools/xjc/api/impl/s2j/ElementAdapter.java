/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.api.impl.s2j;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JInvocation;

/**
 * {@link FieldOutline} that wraps another {@link FieldOutline}
 * and allows JAX-WS to access values without using about
 * {@link JAXBElement}.
 *
 * <p>
 * That means if a value is requested, we unwrap JAXBElement
 * and give it to them. If a value is set, we wrap that into
 * JAXBElement, etc.
 *
 * <p>
 * This can be used only with {@link CReferencePropertyInfo}
 * (or else it won't be {@link JAXBElement),
 * with one {@link CElementInfo} (or else we can't infer the tag name.)
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ElementAdapter implements FieldOutline {
    protected final FieldOutline core;

    /**
     * The only one {@link CElementInfo} that can be in the property.
     */
    protected final CElementInfo ei;

    public ElementAdapter(FieldOutline core, CElementInfo ei) {
        this.core = core;
        this.ei = ei;
    }

    public ClassOutline parent() {
        return core.parent();
    }

    public CPropertyInfo getPropertyInfo() {
        return core.getPropertyInfo();
    }

    protected final Outline outline() {
        return core.parent().parent();
    }

    protected final JCodeModel codeModel() {
        return outline().getCodeModel();
    }

    protected abstract class FieldAccessorImpl implements FieldAccessor {
        final FieldAccessor acc;

        public FieldAccessorImpl(JExpression target) {
            acc = core.create(target);
        }

        public void unsetValues(JBlock body) {
            acc.unsetValues(body);
        }

        public JExpression hasSetValue() {
            return acc.hasSetValue();
        }

        public FieldOutline owner() {
            return ElementAdapter.this;
        }

        public CPropertyInfo getPropertyInfo() {
            return core.getPropertyInfo();
        }

        /**
         * Wraps a type value into a {@link JAXBElement}.
         */
        protected final JInvocation createJAXBElement(JExpression $var) {
            JCodeModel cm = codeModel();

            return JExpr._new(cm.ref(JAXBElement.class))
                .arg(JExpr._new(cm.ref(QName.class))
                    .arg(ei.getElementName().getNamespaceURI())
                    .arg(ei.getElementName().getLocalPart()))
                .arg(getRawType().boxify().erasure().dotclass())
                .arg($var);
        }
    }
}
