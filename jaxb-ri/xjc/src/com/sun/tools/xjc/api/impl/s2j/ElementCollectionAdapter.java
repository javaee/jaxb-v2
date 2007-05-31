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

package com.sun.tools.xjc.api.impl.s2j;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CElementInfo;
import static com.sun.tools.xjc.outline.Aspect.EXPOSED;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * {@link ElementAdapter} that works with a collection
 * of {@link JAXBElement}.
 *
 * @author Kohsuke Kawaguchi
 */
final class ElementCollectionAdapter extends ElementAdapter {
    public ElementCollectionAdapter(FieldOutline core, CElementInfo ei) {
        super(core, ei);
    }

    public JType getRawType() {
        return codeModel().ref(List.class).narrow(itemType().boxify());
    }

    private JType itemType() {
        return ei.getContentInMemoryType().toType(outline(), EXPOSED);
    }

    public FieldAccessor create(JExpression targetObject) {
        return new FieldAccessorImpl(targetObject);
    }

    final class FieldAccessorImpl extends ElementAdapter.FieldAccessorImpl {
        public FieldAccessorImpl(JExpression target) {
            super(target);
        }

        public void toRawValue(JBlock block, JVar $var) {
            JCodeModel cm = outline().getCodeModel();
            JClass elementType = ei.toType(outline(),EXPOSED).boxify();

            // [RESULT]
            // $var = new ArrayList();
            // for( JAXBElement e : [core.toRawValue] ) {
            //   if(e==null)
            //     $var.add(null);
            //   else
            //     $var.add(e.getValue());
            // }

            block.assign($var,JExpr._new(cm.ref(ArrayList.class).narrow(itemType().boxify())));
            JVar $col = block.decl(core.getRawType(), "col" + hashCode());
            acc.toRawValue(block,$col);
            JForEach loop = block.forEach(elementType, "v" + hashCode()/*unique string handling*/, $col);

            JConditional cond = loop.body()._if(loop.var().eq(JExpr._null()));
            cond._then().invoke($var,"add").arg(JExpr._null());
            cond._else().invoke($var,"add").arg(loop.var().invoke("getValue"));
        }

        public void fromRawValue(JBlock block, String uniqueName, JExpression $var) {
            JCodeModel cm = outline().getCodeModel();
            JClass elementType = ei.toType(outline(),EXPOSED).boxify();

            // [RESULT]
            // $t = new ArrayList();
            // for( Type e : $var ) {
            //     $var.add(new JAXBElement(e));
            // }
            // [core.fromRawValue]

            JClass col = cm.ref(ArrayList.class).narrow(elementType);
            JVar $t = block.decl(col,uniqueName+"_col",JExpr._new(col));

            JForEach loop = block.forEach(itemType(), uniqueName+"_i", $t);
            loop.body().invoke($var,"add").arg(createJAXBElement(loop.var()));

            acc.fromRawValue(block, uniqueName, $t);
        }
    }
}
