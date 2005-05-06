/*
 * @(#)$Id: ConstField.java,v 1.2 2005-05-06 21:24:16 kohsuke Exp $
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
        $ref = outline.ref.field(JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
            ptype!=null?ptype:implType, prop.getName(false), prop.defaultValue );
        $ref.javadoc().append(prop.javadoc);
        
        annotate($ref);
    }
    
    public JBlock getOnSetEventHandler() {
        // since this is a constant field, we will never fire this event.
        // just return a dummy block.
        return new JBlock();
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

        public void toArray( JBlock block, JExpression $array ) {
//            if(isCollection) {
//                block.add(
//                    codeModel.ref(System.class).staticInvoke("arraycopy")
//                        .arg($ref).arg(JExpr.lit(0)).arg($array).arg(JExpr.lit(0))
//                        .arg($ref.ref("length")));
//            } else {
                block.assign( $array.component(JExpr.lit(0)), $ref );
//            }
        }
        public void unsetValues( JBlock body ) {
            ;   // can't unset values
        }
        public JExpression hasSetValue() {
            return null;    // can't generate the isSet/unset methods
        }
        public JExpression getContentValue() {
            return $ref;
        }

        public void add(JBlock body, JExpression newValue) {
            ; // can't override any value
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