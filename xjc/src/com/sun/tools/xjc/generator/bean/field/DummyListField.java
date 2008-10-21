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

import com.sun.codemodel.JAnnotatable;
import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.xml.bind.annotation.OverrideAnnotationOf;

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
public class DummyListField extends AbstractListField {

    /**
     * A concrete class that imp    lements the List interface.
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
    protected DummyListField(ClassOutlineImpl context, CPropertyInfo prop, JClass coreList) {
        // the JAXB runtime picks ArrayList if the signature is List,
        // so don't do eager allocation if it's ArrayList.
        // otherwise we need to do eager allocation so that the collection type specified by the user
        // will be used.
        super(context, prop, !coreList.fullName().equals("java.util.ArrayList"));
        this.coreList = coreList.narrow(exposedType.boxify());
        generate();
    }

    /**
     * Annotate the field according to the recipes given as {@link CPropertyInfo}.
     */
    @Override
    protected void annotate( JAnnotatable field ) {
        super.annotate(field);

        if (prop instanceof CReferencePropertyInfo) {
            CReferencePropertyInfo pref = (CReferencePropertyInfo)prop;
            if (pref.isDummy()) {
                annotateDummy(field);
            }
        }

    }

    private void annotateDummy(JAnnotatable field) {
        field.annotate(OverrideAnnotationOf.class);
    }

    protected final JClass getCoreListType() {
        return coreList;
    }

    @Override
    public void generateAccessors() { }

    public Accessor create(JExpression targetObject) {
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
