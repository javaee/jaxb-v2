/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStringLiteral;
import com.sun.xml.bind.WhiteSpaceProcessor;

/**
 * Generates code that performs the whitespace normalization.
 */
public abstract class WhitespaceNormalizer
{
    /**
     * Generates the expression that normalizes
     * the given expression (which evaluates to java.lang.String).
     * 
     * @param codeModel
     *      The owner code model object under which a new expression
     *      will be created. 
     */
    public abstract JExpression generate( JCodeModel codeModel, JExpression literal );
    
    /**
     * Parses "preserve","replace" or "collapse" into
     * the corresponding WhitespaceNormalizer object.
     * 
     * @param method
     *      Either "preserve", "replace", or "collapse"
     * 
     * @exception    IllegalArgumentException
     *        when the specified method is invalid.
     */
    public static WhitespaceNormalizer parse( String method ) {
        if( method.equals("preserve") )
            return PRESERVE;
        
        if( method.equals("replace") )
            return REPLACE;
        
        if( method.equals("collapse") )
            return COLLAPSE;
        
        throw new IllegalArgumentException(method);
    }
    
    public static final WhitespaceNormalizer PRESERVE = new WhitespaceNormalizer() {
        public JExpression generate( JCodeModel codeModel, JExpression literal ) {
            return literal;
        }
    };
    
    public static final WhitespaceNormalizer REPLACE = new WhitespaceNormalizer() {
        public JExpression generate( JCodeModel codeModel, JExpression literal ) {
            // WhitespaceProcessor.replace(<literal>);
            if( literal instanceof JStringLiteral )
                // optimize
                return JExpr.lit( WhiteSpaceProcessor.replace(((JStringLiteral)literal).str) );
            else
                return codeModel.ref(WhiteSpaceProcessor.class)
                    .staticInvoke("replace").arg(literal);
        }
    };
    
    public static final WhitespaceNormalizer COLLAPSE = new WhitespaceNormalizer() {
        public JExpression generate( JCodeModel codeModel, JExpression literal ) {
            // WhitespaceProcessor.replace(<literal>);
            if( literal instanceof JStringLiteral )
                // optimize
                return JExpr.lit( WhiteSpaceProcessor.collapse(((JStringLiteral)literal).str) );
            else
                return codeModel.ref(WhiteSpaceProcessor.class)
                    .staticInvoke("collapse").arg(literal);
        }
    };
}
