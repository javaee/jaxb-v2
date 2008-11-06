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

package com.sun.tools.xjc.generator.bean;


import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ElementOutline;

/**
 * {@link ElementOutline} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
final class ElementOutlineImpl extends ElementOutline {
    private final BeanGenerator parent;

    public BeanGenerator parent() {
        return parent;
    }

    /*package*/ ElementOutlineImpl(BeanGenerator parent, CElementInfo ei) {
        super(ei,
              parent.getClassFactory().createClass(
                      parent.getContainer( ei.parent, Aspect.EXPOSED ), ei.shortName(), ei.getLocator() ));
        this.parent = parent;
        parent.elements.put(ei,this);

        JCodeModel cm = parent.getCodeModel();

        implClass._extends(
            cm.ref(JAXBElement.class).narrow(
                target.getContentInMemoryType().toType(parent,Aspect.EXPOSED).boxify()));

        if(ei.hasClass()) {
            JType implType = ei.getContentInMemoryType().toType(parent,Aspect.IMPLEMENTATION);
            JExpression declaredType = JExpr.cast(cm.ref(Class.class),implType.boxify().dotclass()); // why do we have to cast?
            JClass scope=null;
            if(ei.getScope()!=null)
                scope = parent.getClazz(ei.getScope()).implRef;
            JExpression scopeClass = scope==null?JExpr._null():scope.dotclass();
            JFieldVar valField = implClass.field(JMod.PROTECTED|JMod.FINAL|JMod.STATIC,QName.class,"NAME",createQName(cm,ei.getElementName()));

            // take this opportunity to generate a constructor in the element class
            JMethod cons = implClass.constructor(JMod.PUBLIC);
            cons.body().invoke("super")
                .arg(valField)
                .arg(declaredType)
                .arg(scopeClass)
                .arg(cons.param(implType,"value"));

            // generate no-arg constructor in the element class (bug #391; section 5.6.2 in JAXB spec 2.1)
            JMethod noArgCons = implClass.constructor(JMod.PUBLIC);
            noArgCons.body().invoke("super")
                .arg(valField)
                .arg(declaredType)
                .arg(scopeClass)
                .arg(JExpr._null());

        }
    }

    /**
     * Generates an expression that evaluates to "new QName(...)"
     */
    private JInvocation createQName(JCodeModel codeModel,QName name) {
        return JExpr._new(codeModel.ref(QName.class)).arg(name.getNamespaceURI()).arg(name.getLocalPart());
    }
}
