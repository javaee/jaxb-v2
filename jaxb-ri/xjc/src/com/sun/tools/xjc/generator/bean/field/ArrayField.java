/*
 * @(#)$Id: ArrayField.java,v 1.2 2005-05-06 21:24:16 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldAccessor;

/**
 * Realizes a property as an "indexed property"
 * as specified in the JAXB spec.
 * 
 * <p>
 * We will generate the following set of methods:
 * <pre>
 * T[] getX();
 * T getX( int idx );
 * void setX(T[] values);
 * void setX( int idx, T value );
 * </pre>
 * 
 * We still use List as our back storage.
 * This renderer also handles boxing/unboxing if
 * T is a boxed type.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ArrayField extends AbstractListField {
    
    class Accessor extends AbstractListField.Accessor {
        protected Accessor( JExpression $target ) {
            super($target);
        }
        
        public void toRawValue(JBlock block, JVar $var) {
            block.assign($var,codeModel.ref(Arrays.class).staticInvoke("asList").arg($target.invoke($getAll)));
        }

        public void fromRawValue(JBlock block, String uniqueName, JExpression $var) {
            block.invoke($target,$setAll).arg($var.invoke("toArray").arg(JExpr.newArray(exposedType,$var.invoke("size"))));
        }
    }
    
    private JMethod $setAll;
    
    private JMethod $getAll;
    
    ArrayField(ClassOutlineImpl context, CPropertyInfo prop) {
        super(context,prop);
        generate();
    }
    
    public void generateAccessors() {
        
        MethodWriter writer = outline.createMethodWriter();
        Accessor acc = (Accessor)create(JExpr._this());
        
        JVar $idx,$value; JBlock body;
        JType arrayType = exposedType.array();
        
        // [RESULT] T[] getX() {
        // #ifdef default value
        //     if( !<var>.isModified() ) {
        //         T[] r = new T[defaultValues.length];
        //         System.arraycopy( defaultValues, 0, r, 0, r,length );
        //         return r;
        //     }
        // #else
        //     if( <var>==null )    return new T[0];
        // #endif
        //     return (T[]) <var>.toArray(new T[<var>.size()]);
        // }
        $getAll = writer.declareMethod( exposedType.array(),"get"+prop.getName(true));
        writer.javadoc().append(prop.javadoc);
        body = $getAll.body();
        
        if($defValues!=null) {
            JBlock then = body._if( acc.hasSetValue().not() )._then();
            JVar $r = then.decl( exposedType.array(), "r",JExpr.newArray(exposedType, $defValues.ref("length")));
            
            // [RESULT]
            // System.arraycopy( defaultValues, 0, r, 0, defaultValues.length );
            then.staticInvoke( codeModel.ref(System.class), "arraycopy")
                .arg( $defValues ).arg( JExpr.lit(0) )
                .arg( $r ).arg( JExpr.lit(0) ).arg( $defValues.ref("length") );
  //            } else {
  //                // need to copy them manually to unbox values
  //                // [RESULT]
  //                // for( int i=0; i<r.length; i++ )
  //                //     r[i] = defaultValues[i];
  //                JForLoop loop = then._for();
  //                JVar $i = loop.init(codeModel.INT,"__i",JExpr.lit(0));
  //                loop.test($i.lt($r.ref("length")));
  //                loop.update($i.incr());
  //                loop.body().assign( $r.component($i), unbox($defValues.component($i)) );
  //            }
            then._return($r);   
        } else {
            body._if( acc.ref(true).eq(JExpr._null()) )._then()
                ._return(JExpr.newArray(exposedType,0));
        }
        
        if(primitiveType==null) {
            body._return(JExpr.cast(arrayType,
                acc.ref(true).invoke("toArray").arg( JExpr.newArray(implType,acc.ref(true).invoke("size")) )));
        } else {
            // need to copy them manually to unbox values
            // [RESULT]
            // T[] r = new T[<ref>.size()];
            // for( int i=0; i<r.length; i++ )
            //     r[i] = unbox(<ref>.get(i));
            JVar $r = body.decl(exposedType.array(),"r",JExpr.newArray(exposedType, acc.ref(true).invoke("size")));
            JForLoop loop = body._for();
            JVar $i = loop.init(codeModel.INT,"__i",JExpr.lit(0));
            loop.test($i.lt($r.ref("length")));
            loop.update($i.incr());
            loop.body().assign( $r.component($i),
                primitiveType.unwrap(acc.ref(true).invoke("get").arg($i)) );
            body._return($r);
        }

        List<Object> returnTypes = listPossibleTypes(prop);
        writer.javadoc().addReturn("array of\n").addReturn(returnTypes);
                        
        // [RESULT]
        // ET getX(int idx) {
        // #ifdef default value
        //     if( !<var>.isModified() ) {
        //         return defaultValues[idx];
        //     }
        // #else
        //     if( <var>==null )    throw new IndexOutOfBoundsException();
        // #endif
        //     return unbox(<var>.get(idx));
        // }
        JMethod $get = writer.declareMethod(exposedType,"get"+prop.getName(true));
        $idx = writer.addParameter(codeModel.INT,"idx");
        
        if($defValues!=null) {
            JBlock then = $get.body()._if( acc.hasSetValue().not() )._then();
            then._return($defValues.component($idx));
        } else {
            $get.body()._if(acc.ref(true).eq(JExpr._null()))._then()
                ._throw(JExpr._new(codeModel.ref(IndexOutOfBoundsException.class)));
        }
                    
        writer.javadoc().append(prop.javadoc);
        $get.body()._return(acc.unbox(acc.ref(true).invoke("get").arg($idx) ));

        writer.javadoc().addReturn("one of\n").addReturn(returnTypes);

                        
        // [RESULT] int getXLength() {
        // #ifdef default values
        //     if( !storage.isModified() )
        //         return defaultValues.length;
        // #else
        //     if( <var>==null )    throw new IndexOutOfBoundsException();
        // #endif
        //     return <ref>.size();
        // }
        JMethod $getLength = writer.declareMethod(codeModel.INT,"get"+prop.getName(true)+"Length");
        if($defValues!=null) {
            $getLength.body()._if( acc.hasSetValue().not() )._then()
                ._return($defValues.ref("length"));
        } else {
            $getLength.body()._if(acc.ref(true).eq(JExpr._null()))._then()
                ._return(JExpr.lit(0));
        }
        $getLength.body()._return(acc.ref(true).invoke("size"));
        
                        
        // [RESULT] void setX(ET[] values) {
        //     clear();
        //     int len = values.length;
        //     for( int i=0; i<len; i++ )
        //         <ref>.add(values[i]);
        // }
        $setAll = writer.declareMethod(
            codeModel.VOID,
            "set"+prop.getName(true));
        
        writer.javadoc().append(prop.javadoc);
        
        $value = writer.addParameter(exposedType.array(),"values");
        $setAll.body().invoke(acc.ref(false),"clear");
        JVar $len = $setAll.body().decl(codeModel.INT,"len", $value.ref("length"));
        JForLoop _for = $setAll.body()._for();
        JVar $i = _for.init( codeModel.INT, "i", JExpr.lit(0) );
        _for.test( JOp.lt($i,$len) );
        _for.update( $i.incr() );
        _for.body().invoke(acc.ref(true),"add").arg(castToImplType(acc.box($value.component($i))));

        writer.javadoc()
                .addParam($value,"allowed objects are\n")
                .addParam($value,returnTypes);
                        
        // [RESULT] ET setX(int,ET)
        JMethod $set = writer.declareMethod(
            exposedType,
            "set"+prop.getName(true));
        $idx = writer.addParameter( codeModel.INT, "idx" );
        $value = writer.addParameter( exposedType, "value" );

        writer.javadoc().append(prop.javadoc);
                        
        body = $set.body();
        body._return( acc.unbox(
            acc.ref(true).invoke("set").arg($idx).arg(castToImplType(acc.box($value)))));

        writer.javadoc()
                .addParam($value,"allowed object is\n")
                .addParam($value,returnTypes);
    }
    
    protected JClass getCoreListType() {
        return codeModel.ref(ArrayList.class).narrow(exposedType.boxify());
    }
    
    public FieldAccessor create(JExpression targetObject) {
        return new Accessor(targetObject);
    }
}