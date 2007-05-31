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

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.outline.ClassOutline;

/**
 * The back-end may or may not generate the content interface
 * separately from the implementation class. If so, a method
 * needs to be declared on both the interface and the implementation class.
 * <p>
 * This class hides those details and allow callers to declare
 * methods just once.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class MethodWriter {
    protected final JCodeModel codeModel;
    
    protected MethodWriter(ClassOutline context) {
        this.codeModel = context.parent().getCodeModel();
    }

    /**
     * Declares a method in both the interface and the implementation.
     * 
     * @return
     *      JMethod object that represents a newly declared method
     *      on the implementation class.
     */
    public abstract JMethod declareMethod( JType returnType, String methodName );
    
    public final JMethod declareMethod( Class returnType, String methodName ) {
        return declareMethod( codeModel.ref(returnType), methodName );
    }
    
    /**
     * To generate javadoc for the previously declared method, use this method
     * to obtain a {@link JDocComment} object. This may return a value
     * different from declareMethod().javadoc().
     */
    public abstract JDocComment javadoc();

            
    /**
     * Adds a parameter to the previously declared method.
     * 
     * @return
     *      JVar object that represents a newly added parameter
     *      on the implementation class.
     */
    public abstract JVar addParameter( JType type, String name );
    
    public final JVar addParameter( Class type, String name ) {
        return addParameter( codeModel.ref(type), name );
    }
}
