/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.lang.annotation.Annotation;

/**
 * Enum Constant.
 *
 * When used as an {@link JExpression}, this object represents a reference to the enum constant.
 *
 * @author
 *     Bhakti Mehta (Bhakti.Mehta@sun.com)
 */
public final class JEnumConstant extends JExpressionImpl implements JDeclaration, JAnnotatable, JDocCommentable {

    /**
     * The constant.
     */
    private final String name;
    /**
     * The enum class.
     */
    private final JDefinedClass type;
    /**
     * javadoc comments, if any.
     */
    private JDocComment jdoc = null;

    /**
     * Annotations on this variable. Lazily created.
     */
    private List<JAnnotationUse> annotations = null;


    /**
     * List of the constructor argument expressions.
     * Lazily constructed.
     */
    private List<JExpression> args = null;

    JEnumConstant(JDefinedClass type,String name) {
        this.name = name;
        this.type = type;
    }

    /**
     *  Add an expression to this constructor's argument list
     *
     * @param arg
     *        Argument to add to argument list
     */
    public JEnumConstant arg(JExpression arg) {
        if(arg==null)   throw new IllegalArgumentException();
        if(args==null)
            args = new ArrayList<JExpression>();
        args.add(arg);
        return this;
    }

    /**
     * Returns the name of this constant.
     *
     * @return never null.
     */
    public String getName() {
    	return this.type.fullName().concat(".").concat(this.name);
    }

    /**
     * Creates, if necessary, and returns the enum constant javadoc.
     *
     * @return JDocComment containing javadocs for this constant.
     */
    public JDocComment javadoc() {
        if (jdoc == null)
            jdoc = new JDocComment(type.owner());
        return jdoc;
    }

    /**
     * Adds an annotation to this variable.
     * @param clazz
     *          The annotation class to annotate the field with
     */
    public JAnnotationUse annotate(JClass clazz){
        if(annotations==null)
           annotations = new ArrayList<JAnnotationUse>();
        JAnnotationUse a = new JAnnotationUse(clazz);
        annotations.add(a);
        return a;
    }

    /**
     * Adds an annotation to this variable.
     *
     * @param clazz
     *          The annotation class to annotate the field with
     */
    public JAnnotationUse annotate(Class <? extends Annotation> clazz){
        return annotate(type.owner().ref(clazz));
    }

    public <W extends JAnnotationWriter> W annotate2(Class<W> clazz) {
        return TypedAnnotationWriter.create(clazz,this);
    }

    /**
     * {@link JAnnotatable#annotations()}
     */
    public Collection<JAnnotationUse> annotations() {
        if (annotations == null)
            annotations = new ArrayList<JAnnotationUse>();
        return Collections.unmodifiableList(annotations);
    }

    public void declare(JFormatter f) {
        if( jdoc != null )
            f.nl().g( jdoc );
        if (annotations != null) {
            for( int i=0; i<annotations.size(); i++ )
                f.g(annotations.get(i)).nl();
        }
        f.id(name);
        if(args!=null) {
            f.p('(').g(args).p(')');
        }
    }

    public void generate(JFormatter f) {
    	f.t(type).p('.').p(name);
    }
}
