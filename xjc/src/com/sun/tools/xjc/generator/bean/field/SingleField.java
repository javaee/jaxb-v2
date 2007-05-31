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

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.xml.bind.api.impl.NameConverter;

/**
 * Realizes a property through one getter and one setter.
 * This renders:
 * 
 * <pre>
 * T' field;
 * T getXXX() { ... }
 * void setXXX(T value) { ... }
 * </pre>
 *
 * <p>
 * Normally T'=T, but under some tricky circumstances they could be different
 * (like T'=Integer, T=int.)
 *
 * This realization is only applicable to fields with (1,1)
 * or (0,1) multiplicity.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SingleField extends AbstractFieldWithVar {

    protected SingleField(ClassOutlineImpl context, CPropertyInfo prop) {
        this(context,prop,false);
    }

    /**
     *
     * @param forcePrimitiveAccess
     *      forces the setter/getter to expose the primitive type.
     *      it's a pointless customization, but it's nevertheless in the spec.
     */
    protected SingleField(ClassOutlineImpl context, CPropertyInfo prop, boolean forcePrimitiveAccess ) {
        super(context, prop);
        assert !exposedType.isPrimitive() && !implType.isPrimitive();
        
        createField();
        
        MethodWriter writer = context.createMethodWriter();
        NameConverter nc = context.parent().getModel().getNameConverter();

        // [RESULT]
        // Type getXXX() {
        // #ifdef default value
        //     if(value==null)
        //         return defaultValue;
        // #endif
        //     return value;
        // }
        JExpression defaultValue = null;
        if(prop.defaultValue!=null)
            defaultValue = prop.defaultValue.compute(outline.parent());

        // if Type is a wrapper and we have a default value,
        // we can use the primitive type.
        JType getterType;
        if(defaultValue!=null || forcePrimitiveAccess)
            getterType = exposedType.unboxify();
        else
            getterType = exposedType;

        JMethod $get = writer.declareMethod( getterType,getGetterMethod() );
        String javadoc = prop.javadoc;
        if(javadoc.length()==0)
            javadoc = Messages.DEFAULT_GETTER_JAVADOC.format(nc.toVariableName(prop.getName(true)));
        writer.javadoc().append(javadoc);


        if(defaultValue==null) {
            $get.body()._return(ref());
        } else {
            JConditional cond = $get.body()._if(ref().eq(JExpr._null()));
            cond._then()._return(defaultValue);
            cond._else()._return(ref());
        }

        List<Object> possibleTypes = listPossibleTypes(prop);
        writer.javadoc().addReturn()
            .append("possible object is\n")
            .append(possibleTypes);
         
        // [RESULT]
        // void setXXX(Type newVal) {
        //     this.value = newVal;
        // }
        JMethod $set = writer.declareMethod( codeModel.VOID, "set"+prop.getName(true) );
        JType setterType = exposedType;
        if(forcePrimitiveAccess)    setterType = setterType.unboxify();
        JVar $value = writer.addParameter( setterType, "value" );
        JBlock body = $set.body();
        body.assign(JExpr._this().ref(ref()),castToImplType($value));

        javadoc = prop.javadoc;
        if(javadoc.length()==0)
            javadoc = Messages.DEFAULT_SETTER_JAVADOC.format(nc.toVariableName(prop.getName(true)));
        writer.javadoc().append(javadoc);
        writer.javadoc().addParam($value)
            .append("allowed object is\n")
            .append(possibleTypes);
    }

    public final JType getFieldType() {
        return implType;
    }

    public FieldAccessor create(JExpression targetObject) {
        return new Accessor(targetObject);
    }
    
    protected class Accessor extends AbstractFieldWithVar.Accessor {
        protected Accessor(JExpression $target) {
            super($target);
        }
        
        public void unsetValues( JBlock body ) {
            body.assign( $ref, JExpr._null() );
        }
        public JExpression hasSetValue() {
            return $ref.ne( JExpr._null() );
        }
    }
}
