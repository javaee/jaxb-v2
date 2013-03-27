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

package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldAccessor;

/**
 * Realizes a property as a "public static final" property on the interface.
 * This class can handle both boxed/unboxed types and both
 * single/colllection.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ConstField extends AbstractField {
//    /**
//     * Number of items in this property, when
//     * {@link #isCollection}==true.
//     */
//    private final int count=1;

    /** Generated constant property on the interface. */
    private final JFieldVar $ref;

    ConstField( ClassOutlineImpl outline, CPropertyInfo prop ) {
        super(outline,prop);

        // we only support value constraints for a single-value property.
        assert !prop.isCollection();

        JPrimitiveType ptype = implType.boxify().getPrimitiveType();

        // generate the constant
        JExpression defaultValue = null;
        if(prop.defaultValue!=null)
            defaultValue = prop.defaultValue.compute(outline.parent());

        $ref = outline.ref.field(JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
            ptype!=null?ptype:implType, prop.getName(true), defaultValue );
        $ref.javadoc().append(prop.javadoc);
        
        annotate($ref);
    }
    
    public JType getRawType() {
//        if( isCollection )      return getInfo().array();
        return exposedType;
    }
    
    
    public FieldAccessor create(JExpression target) {
        return new Accessor(target);
    }
    
    private class Accessor extends AbstractField.Accessor {
        
        Accessor( JExpression $target ) {
            super($target);
        }

        public void unsetValues( JBlock body ) {
            ;   // can't unset values
        }
        public JExpression hasSetValue() {
            return null;    // can't generate the isSet/unset methods
        }
        public void toRawValue(JBlock block, JVar $var) {
            // TODO: rethink abstraction. Those constant fields
            // don't have "access" to them.
            throw new UnsupportedOperationException();
        }

        public void fromRawValue(JBlock block, String uniqueName, JExpression $var) {
            throw new UnsupportedOperationException();
        }
    }
}
