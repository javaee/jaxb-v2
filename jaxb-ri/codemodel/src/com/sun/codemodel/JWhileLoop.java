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


/**
 * While statement
 */

public class JWhileLoop implements JStatement {

    /**
     * Test part of While statement for determining exit state
     */
    private JExpression test;

    /**
     * JBlock of statements which makes up body of this While statement
     */
    private JBlock body = null;

    /**
     * Construct a While statment
     */
    JWhileLoop(JExpression test) {
        this.test = test;
    }

    public JExpression test() {
        return test;
    }

    public JBlock body() {
        if (body == null) body = new JBlock();
        return body;
    }

    public void state(JFormatter f) {
        if (JOp.hasTopOp(test)) {
            f.p("while ").g(test);
        } else {
            f.p("while (").g(test).p(')');
        }
        if (body != null)
            f.s(body);
        else
            f.p(';').nl();
    }

}
