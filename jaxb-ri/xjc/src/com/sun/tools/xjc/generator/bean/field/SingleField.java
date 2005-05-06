/*
 * @(#)$Id: SingleField.java,v 1.3 2005-05-06 21:49:18 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
import com.sun.xml.bind.v2.NameConverter;

/**
 * Realizes a property through one getter and one setter.
 * This rendered can be used only with a reference type
 * 
 * <pre>
 * T getXXX();
 * void setXXX(T value);
 * </pre>
 * 
 * This realization is only applicable to fields with (1,1)
 * or (0,1) multiplicity.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class SingleField extends AbstractFieldWithVar {
    
    SingleField(ClassOutlineImpl context, CPropertyInfo prop) {
        super(context, prop);
        assert !exposedType.isPrimitive() && !implType.isPrimitive();
        
        createField();
        
        MethodWriter writer = context.createMethodWriter();
        NameConverter nc = context.parent().getModel().getNameConverter();

        /**
         * Generates the following get/set methods.
         * <pre>
         * T getXXX();
         * void setXXX(T value);
         * </pre>
         */
        
        // [RESULT]
        // Type getXXX() {
        // #ifdef default value
        //     if(value==null)
        //         return defaultValue;
        // #endif
        //     return value;
        // }
        JExpression defaultValue = prop.defaultValue;

        // if Type is a wrapper and we have a default value,
        // we can use the primitive type.
        JType getterType;
        if(defaultValue!=null && exposedType.boxify().getPrimitiveType()!=null)
            getterType = exposedType.boxify().getPrimitiveType();
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
        //     
        //     /*onSetEventHandler*/
        // }       
        JMethod $set = writer.declareMethod( codeModel.VOID, "set"+prop.getName(true) );
        JVar $value = writer.addParameter( exposedType, "value" );
        JBlock body = $set.body();
        body.assign(JExpr._this().ref(ref()),castToImplType($value));
        onSetEvent = body;
        
        javadoc = prop.javadoc;
        if(javadoc.length()==0)
            javadoc = Messages.DEFAULT_SETTER_JAVADOC.format(nc.toVariableName(prop.getName(true)));
        writer.javadoc().append(javadoc);
        writer.javadoc().addParam($value)
            .append("allowed object is\n")
            .append(possibleTypes);
    }

    /**
     * Code fragment that gets executed when the set method
     * is called. IOW, this is an event handler of the "onSet" event.
     */
    private JBlock onSetEvent;

    public final JType getFieldType() {
        return implType;
    }

    public final JBlock getOnSetEventHandler() {
        return onSetEvent;
    }

    public FieldAccessor create(JExpression targetObject) {
        return new Accessor(targetObject);
    }
    
    protected class Accessor extends AbstractFieldWithVar.Accessor {
        protected Accessor(JExpression $target) {
            super($target);
        }
        
        public void add( JBlock block, JExpression newValue ) {
            block.assign($ref,newValue);               
        }

        public void toArray( JBlock block, JExpression $array ) {
            block.assign( $array.component(JExpr.lit(0)), $ref );
        }
        
        public void unsetValues( JBlock body ) {
            body.assign( $ref, JExpr._null() );
        }
        public JExpression hasSetValue() {
            return $ref.ne( JExpr._null() );
        }
        public JExpression getContentValue() {
            return $ref;
        }
    }
}
