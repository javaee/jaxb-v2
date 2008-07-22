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
package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractFieldWithVar extends AbstractField {
    
    /**
     * Field declaration of the actual list object that we use
     * to store data.
     */
    private JFieldVar field;
    
    /**
     * Invoke {@link #createField()} after calling the
     * constructor.
     */
    AbstractFieldWithVar( ClassOutlineImpl outline, CPropertyInfo prop ) {
        super(outline,prop);
    }
    
    protected final void createField() {
        field = outline.implClass.field( JMod.PROTECTED,
            getFieldType(), prop.getName(false) );

        annotate(field);
    }

    /**
     * Gets the name of the getter method.
     *
     * <p>
     * This encapsulation is necessary because sometimes we use
     * {@code isXXXX} as the method name.
     */
    protected String getGetterMethod() {
        return ((getFieldType().isPrimitive() &&
                 getFieldType().boxify().getPrimitiveType()==codeModel.BOOLEAN) ?
                     "is":"get") + prop.getName(true);
    }

    /**
     * Returns the type used to store the value of the field in memory.
     */
    protected abstract JType getFieldType();

    protected JFieldVar ref() { return field; }

    public final JType getRawType() {
        return exposedType;
    }
    
    protected abstract class Accessor extends AbstractField.Accessor {
    
        protected Accessor(JExpression $target) {
            super($target);
            this.$ref = $target.ref(AbstractFieldWithVar.this.ref());
        }
        
        /**
         * Reference to the field bound by the target object.
         */
        protected final JFieldRef $ref;

        public final void toRawValue(JBlock block, JVar $var) {
            block.assign($var,$target.invoke(getGetterMethod()));
        }

        public final void fromRawValue(JBlock block, String uniqueName, JExpression $var) {
            block.invoke($target,("set"+prop.getName(true))).arg($var);
        }
    }
}
