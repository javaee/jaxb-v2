/*
 * @(#)$Id: UnboxedField.java,v 1.2 2005-05-06 21:24:16 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.xml.bind.v2.NameConverter;

/**
 * A required primitive property.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class UnboxedField extends AbstractFieldWithVar {

    /**
     * Code fragment that gets executed when the set method
     * is called. IOW, this is an event handler of the "onSet" event.
     */
    private JBlock onSetEvent;

    /**
     * The primitive version of {@link #implType} and {@link #exposedType}.
     */
    private final JPrimitiveType ptype;


    UnboxedField( ClassOutlineImpl outline, CPropertyInfo prop ) {
        super(outline,prop);
        // primitive types don't have this distintion
        assert implType==exposedType;

        ptype = (JPrimitiveType) implType;
        assert ptype!=null;
        
        createField();

        assert prop.defaultValue==null;

        MethodWriter writer = outline.createMethodWriter();
        NameConverter nc = outline.parent().getModel().getNameConverter();

        JBlock body;
        
        // [RESULT]
        // Type getXXX() {
        //     return value;
        // }
        JMethod $get = writer.declareMethod( ptype, getGetterMethod() );
        String javadoc = prop.javadoc;
        if(javadoc.length()==0)
            javadoc = Messages.DEFAULT_GETTER_JAVADOC.format(nc.toVariableName(prop.getName(true)));
        writer.javadoc().append(javadoc);

        $get.body()._return(ref());


        // [RESULT]
        // void setXXX( Type value ) {
        //     this.value = value;
        //     /*onSetEventHandler*/
        // }
        JMethod $set = writer.declareMethod( codeModel.VOID, "set"+prop.getName(true) );
        JVar $value = writer.addParameter( ptype, "value" );
        body = $set.body();
        body.assign(JExpr._this().ref(ref()),$value);
        onSetEvent = body;
        javadoc = prop.javadoc;
        if(javadoc.length()==0)
            javadoc = Messages.DEFAULT_SETTER_JAVADOC.format(nc.toVariableName(prop.getName(true)));
        writer.javadoc().append(javadoc);

    }

    protected JType getType(Aspect aspect) {
        return super.getType(aspect).boxify().getPrimitiveType();
    }

    public JBlock getOnSetEventHandler() {
        return onSetEvent;
    }
    
    protected JType getFieldType() {
        return ptype;
    }

    public FieldAccessor create(JExpression targetObject) {
        return new Accessor(targetObject) {
            
            public JExpression getContentValue() {
                return ptype.wrap($ref);
            }

            public void toArray( JBlock block, JExpression $array ) {
                block.assign( $array.component(JExpr.lit(0)), $ref );
            }
            
            
            public void unsetValues( JBlock body ) {
                // you can't unset a value
            }
            
            public JExpression hasSetValue() {
                return JExpr.TRUE;
            }

            public void add( JBlock block, JExpression newValue ) {
                block.assign($ref, newValue);
            }
        };
    }
}
