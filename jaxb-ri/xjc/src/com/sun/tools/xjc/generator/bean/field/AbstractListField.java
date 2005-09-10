/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.generator.bean.field;

import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.xml.bind.v2.TODO;

/**
 * Common code for property renderer that generates a List as
 * its underlying data structure.
 * 
 * <p>
 * For performance reaons, the actual list object used to store
 * data is lazily created.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractListField extends AbstractField {
    // TODO: if we can get rid of default value handling, this will become a whole lot easier!

    /**
     * Expression object that represents how a new List object
     * should be built.
     */
    private JExpression newListObjectExp;
    /**
     * Reference to the array of default values, if there is a default value.
     * Otherwise null.
     */
    protected JVar $defValues = null;
    private JExpression lazyInitializer = new JExpressionImpl() {
        public void generate(JFormatter f) {
            newListObjectExp.generate(f);
        }
    };
    
    /** The field that stores the list. */
    protected JFieldVar field;
    
    /**
     * a method that lazily initializes a List
     * [RESULT]
     * List _getFoo() {
     *   if(field==null)
     *     field = create new list;
     *   return field;
     * }
     */
    protected JMethod internalGetter;
    
    /**
     * If this collection property is a collection of a primitive type,
     * this variable refers to that primitive type.
     * Otherwise null.
     */
    protected final JPrimitiveType primitiveType;

    protected final JClass listT = codeModel.ref(List.class).narrow(exposedType.boxify());

    
    /**
     * Call {@link #generate()} method right after this.
     */
    protected AbstractListField(ClassOutlineImpl outline, CPropertyInfo prop) {
        super(outline,prop);

        if( implType instanceof JPrimitiveType ) {
            // primitive types don't have this tricky distinction
            assert implType==exposedType;
            primitiveType = (JPrimitiveType)implType;
        } else
            primitiveType = null;
    }
    
    protected final void generate() {
        
        field=generateField();
        
        internalGetter = outline.implClass.method(JMod.PROTECTED,listT,"_get"+prop.getName(true));
        internalGetter.body()._if(field.eq(JExpr._null()))._then()
            .assign(field,lazyInitializer);
        internalGetter.body()._return(field);
        
        // generate the rest of accessors
        generateAccessors();
    }
    private JFieldVar generateField() {
        TODO.checkSpec("I hope we decide not to handle the default values for a list.");

        JFieldVar ref = outline.implClass.field( JMod.PROTECTED, listT, prop.getName(false) );
        annotate(ref);
        
        newListObjectExp = newCoreList();
            
//        // generate default values
//        if(defaultValues!=null) {
//            JInvocation initializer;
//            JType arrayType = prop.type.array();
//            // if there are default values, create an array for them.
//
//            // [RESULT] static final protected T[] XX_defaultValues = new T[]{...}
//            $defValues = outline.implClass.field(JMod.STATIC|JMod.FINAL|JMod.PROTECTED,
//                arrayType,prop.getName()+"_defaultValues",
//                initializer=JExpr._new(arrayType));
//
//            for( int i=0; i<defaultValues.length; i++ )
//               initializer.arg( defaultValues[i].generateConstant() );
//        }

        return ref;
    }
    
    public JBlock getOnSetEventHandler() {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }

    public final JType getRawType() {
        return codeModel.ref(List.class).narrow(exposedType.boxify());
    }
    
    private JExpression newCoreList() {
        return JExpr._new(getCoreListType());
    }
    
    /**
     * Concrete class that implements the List interface.
     * Used as the actual data storage.
     */
    protected abstract JClass getCoreListType();
    
    
    /** Generates accessor methods. */
    protected abstract void generateAccessors();
    
    
    
    /**
     * 
     * 
     * @author
     *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
     */
    protected abstract class Accessor extends AbstractField.Accessor {
        
        /**
         * Reference to the {@link AbstractListField#field}
         * of the target object.
         */
        protected final JFieldRef field;
        
        protected Accessor( JExpression $target ) {
            super($target);
            field = $target.ref(AbstractListField.this.field);
        }
        
        
        protected final JExpression unbox( JExpression exp ) {
            if(primitiveType==null) return exp;
            else                    return primitiveType.unwrap(exp);
        }
        protected final JExpression box( JExpression exp ) {
            if(primitiveType==null) return exp;
            else                    return primitiveType.wrap(exp);
        }
        
        /**
         * Returns a reference to the List field that stores the data.
         * <p>
         * Using this method hides the fact that the list is lazily
         * created.
         * 
         * @param canBeNull
         *      if true, the returned expression may be null (this is
         *      when the list is still not constructed.) This could be
         *      useful when the caller can deal with null more efficiently.
         *      When the list is null, it should be treated as if the list
         *      is empty.
         * 
         *      if false, the returned expression will never be null.
         *      This is the behavior users would see.
         */
        protected final JExpression ref(boolean canBeNull) {
            if(canBeNull)
                return field;
            else
                return $target.invoke(internalGetter);
        }

        public void add( JBlock body, JExpression newValue ) {
            if( primitiveType!=null )
                newValue = primitiveType.wrap(newValue);
            body.invoke(ref(false),"add").arg(newValue);
        }

        public void toArray( JBlock block, JExpression $array ) {
            // if the list is null, no need to copy to the array
            block = block._if( field.ne(JExpr._null()) )._then();
            
            if( primitiveType==null ) {
                // [RESULT]
                // list.toArray( array );
                block.invoke( ref(true), "toArray" ).arg($array);
            } else {
                // [RESULT]
                // for( int idx=<length>-1; idx>=0; idx-- ) {
                //     array[idx] = <unbox>(list.get(<idx>));
                // }
                JForLoop $for = block._for();
                JVar $idx = $for.init(codeModel.INT,"q"+this.hashCode(), count().minus(JExpr.lit(1)) );
                $for.test( $idx.gte(JExpr.lit(0)) );
                $for.update( $idx.decr() );
                
                $for.body().assign( $array.component($idx),
                    primitiveType.unwrap(
                        JExpr.cast( primitiveType.boxify(), ref(true).invoke("get").arg($idx) )));
            }
        }

        public JExpression count() {
            return JOp.cond( field.eq(JExpr._null()), JExpr.lit(0), field.invoke("size") );
        }
        
        public void unsetValues( JBlock body ) {
            body.assign(field,JExpr._null());
        }
        public JExpression hasSetValue() {
            return field.ne(JExpr._null()).cand(field.invoke("isEmpty").not());
        }
        public JExpression getContentValue() {
            return ref(false);
        }

    }
    
}
