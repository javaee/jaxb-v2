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
 * A cast operation.
 */
final class JCast extends JExpressionImpl {
    /**
     * JType to which the expression is to be cast.
     */
    private final JType type;

    /**
     * JExpression to be cast.
     */
    private final JExpression object;

    /**
     * JCast constructor 
     *
     * @param type
     *        JType to which the expression is cast
     *
     * @param object
     *        JExpression for the object upon which
     *        the cast is applied
     */
    JCast(JType type, JExpression object) {
        this.type = type;
        this.object = object;
    }

    public void generate(JFormatter f) {
        f.p("((").g(type).p(')').g(object).p(')');
    }
}
