/*
 * @(#)$Id: UntypedListField.java,v 1.2 2005-05-06 21:24:16 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean.field;

import java.util.List;
import java.util.ArrayList;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.MethodWriter;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.xml.bind.v2.NameConverter;

/**
 * Realizes a property as an untyped {@link List}.
 * 
 * <pre>
 * List getXXX();
 * </pre>
 * 
 * <h2>Default value handling</h2>
 * <p>
 * Since unmarshaller just adds new values into the storage,
 * we can't fill the storage by default values at the time of
 * instanciation. (or oherwise values found in the document will
 * be appended to default values, where it should overwrite them.)
 * <p>
 * Therefore, when the object is created, the storage will be empty.
 * When the getXXX method is called, we'll check if the storage is
 * modified in anyway. If it is modified, it must mean that the values
 * are found in the document, so we just return it.
 * 
 * Otherwise we will fill in default values and return it to the user.
 * 
 * <p>
 * When a list has default values, its dirty flag is set to true.
 * Marshaller will check this and treat it appropriately.
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class UntypedListField extends AbstractListField {

    /**
     * A concrete class that implements the List interface.
     * An instance of this class will be used to store data
     * for this field.
     */
    private final JClass coreList;


    /** List getFIELD() method. */
    private JMethod $get;

    /**
     * @param coreList
     *      A concrete class that implements the List interface.
     *      An instance of this class will be used to store data
     *      for this field.
     */
    protected UntypedListField(ClassOutlineImpl context, CPropertyInfo prop, JClass coreList) {
        super(context, prop);
        this.coreList = coreList.narrow(exposedType.boxify());
        generate();
    }

    protected final JClass getCoreListType() {
        return coreList;
    }
    
    public void generateAccessors() {
        
        final MethodWriter writer = outline.createMethodWriter();
        final Accessor acc = (Accessor)create(JExpr._this());
        
        final JExpression refT = acc.ref(true);
        final JExpression refF = acc.ref(false);

        JBlock body;

        // [RESULT]
        // List getXXX() {
        // #ifdef default value
        //     if(!<ref>.isModified() && <ref>.isEmpty() ) {
        //         // fill in the default values
        //         for( int i=0; i<defaultValues.length; i++ )
        //             <ref>.add(box(defaultValues[i]));
        //         <ref>.setModified(false);
        //     }
        // #endif
        //     return <ref>;
        // }
        $get = writer.declareMethod(listT,"get"+prop.getName(true));
        writer.javadoc().append(prop.javadoc);
        body = $get.body();
        if($defValues!=null) {
            JBlock then = body._if(
                JOp.cand( acc.hasSetValue().not(), refF.invoke("isEmpty") ) )._then();
            JForLoop loop = then._for();
            JVar $i = loop.init(codeModel.INT,"__i",JExpr.lit(0));
            loop.test($i.lt($defValues.ref("length")));
            loop.update($i.incr());
            loop.body().invoke(refT,"add").arg(acc.box($defValues.component($i)));
            
            then.invoke(refT,"setModified").arg(JExpr.FALSE);
        }
        body._return(refF);


        String pname = NameConverter.standard.toVariableName(prop.getName(true));
        writer.javadoc().append(
            "Gets the value of the "+pname+" property.\n\n"+
            "<p>\n" +
            "This accessor method returns a reference to the live list,\n" +
            "not a snapshot. Therefore any modification you make to the\n" +
            "returned list will be present inside the JAXB object.\n" +
            "This is why there is not a <CODE>set</CODE> method for the " +pname+ " property.\n" +
            "\n"+
            "<p>\n" +
            "For example, to add a new item, do as follows:\n"+
            "<pre>\n"+
            "   get"+prop.getName(true)+"().add(newItem);\n"+
            "</pre>\n"+
            "\n\n"
        );
        
        writer.javadoc().append(
            "<p>\n" +
            "Objects of the following type(s) are allowed in the list\n")
            .append(listPossibleTypes(prop));

        // [RESULT]
        // #ifdef default value
        // void deleteXXX() {
        //     <ref>.clear();
        //     <ref>.setModified(false);
        // }
        // #endif
        // setModified(false) so that the getXXX method will correctly
        // recognize that we need to fill in default values again.
        if($defValues!=null) {
             JMethod $delete = writer.declareMethod(codeModel.VOID, "delete"+prop.getName(true));
             writer.javadoc().setDeprecated(
                "this method is incorrectly generated by previous\n" +
                "releases of the RI. This method remains here just to make \n" +
                "the generated code backward compatible.\n" +
                "Applications should <strong>NOT</strong> rely on this method, and\n" +
                "if it needs this capability, it should use the unset"+prop.getName(true)+" method.\n" +
                "To generate the unset"+prop.getName(true)+" method, please use \n" +
                "the <code>generateIsSetMethod</code> attribute on\n" +
                "<code>globalBindings</code> or <code>property</code> customization." );
             acc.unsetValues($delete.body());
        }
    }

    public FieldAccessor create(JExpression targetObject) {
        return new Accessor(targetObject);
    }

    class Accessor extends AbstractListField.Accessor {
        protected Accessor( JExpression $target ) {
            super($target);
        }
        
        public void toRawValue(JBlock block, JVar $var) {
            // [RESULT]
            // $<var>.addAll(bean.getLIST());
            // list.toArray( array );
            block.assign($var,JExpr._new(codeModel.ref(ArrayList.class).narrow(exposedType.boxify())).arg(
                $target.invoke($get)
            ));
        }

        public void fromRawValue(JBlock block, String uniqueName, JExpression $var) {
            // [RESULT]
            // bean.getLIST().addAll($<var>);
            JVar $list = block.decl(listT,uniqueName+'l',$target.invoke($get));
            block.invoke($list,"addAll").arg($var);
        }
    }
}
