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

import com.sun.codemodel.JType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.model.CElementInfo;

/**
 * {@link ElementAdapter} that works with a single {@link JAXBElement}.
 *
 * @author Kohsuke Kawaguchi
 */
final class ElementSingleAdapter extends ElementAdapter {
    public ElementSingleAdapter(FieldOutline core, CElementInfo ei) {
        super(core, ei);
    }

    public JType getRawType() {
        return ei.getContentInMemoryType().toType(outline(), Aspect.EXPOSED);
    }

    public FieldAccessor create(JExpression targetObject) {
        return new FieldAccessorImpl(targetObject);
    }

    final class FieldAccessorImpl extends ElementAdapter.FieldAccessorImpl {
        public FieldAccessorImpl(JExpression target) {
            super(target);
        }

        public void toRawValue(JBlock block, JVar $var) {
            // [RESULT]
            // if([core.hasSetValue])
            //   $var = [core.toRawValue].getValue();
            // else
            //   $var = null;

            JConditional cond = block._if(acc.hasSetValue());
            JVar $v = cond._then().decl(core.getRawType(), "v" + hashCode());// TODO: unique value control
            acc.toRawValue(cond._then(),$v);
            cond._then().assign($var,$v.invoke("getValue"));
            cond._else().assign($var, JExpr._null());
        }

        public void fromRawValue(JBlock block, String uniqueName, JExpression $var) {
            // [RESULT]
            // [core.fromRawValue](new JAXBElement(tagName, TYPE, $var));

            acc.fromRawValue(block,uniqueName, createJAXBElement($var));
        }
    }
}
