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

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.List;


/**
 * array creation and initialization.
 */
public final class JArray extends JExpressionImpl {

    private final JType type;
    private final JExpression size;
    private List<JExpression> exprs = null;

    /**
     * Add an element to the array initializer
     */
    public JArray add(JExpression e) {
        if (exprs == null)
            exprs = new ArrayList<JExpression>();
        exprs.add(e);
        return this;
    }

    JArray(JType type, JExpression size) {
        this.type = type;
        this.size = size;
    }

    public void generate(JFormatter f) {
        
        // generally we produce new T[x], but when T is an array type (T=T'[])
        // then new T'[][x] is wrong. It has to be new T'[x][].
        int arrayCount = 0;
        JType t = type;
        
        while( t.isArray() ) {
            t = t.elementType();
            arrayCount++;
        }
        
        f.p("new").g(t).p('[');
        if (size != null)
            f.g(size);
        f.p(']');
        
        for( int i=0; i<arrayCount; i++ )
            f.p("[]");
        
        if ((size == null) || (exprs != null))
            f.p('{');
        if (exprs != null) {
            f.g(exprs);
        } else {
            f.p(' ');
        }
        if ((size == null) || (exprs != null))
            f.p('}');
    }

}
