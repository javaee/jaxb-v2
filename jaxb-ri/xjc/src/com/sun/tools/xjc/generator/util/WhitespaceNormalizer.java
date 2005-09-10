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
