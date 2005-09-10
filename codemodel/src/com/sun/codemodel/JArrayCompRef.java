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
 * array component reference.
 */
final class JArrayCompRef extends JExpressionImpl implements JAssignmentTarget {
    /**
     * JArray expression upon which this component will be accessed.
     */
    private final JExpression array;

    /**
     * Integer expression representing index of the component
     */
    private final JExpression index;

    /**
     * JArray component reference constructor given an array expression
     * and index.
     *
     * @param array
     *        JExpression for the array upon which
     *        the component will be accessed,
     *
     * @param index
     *        JExpression for index of component to access
     */
    JArrayCompRef(JExpression array, JExpression index) {
        if ((array == null) || (index == null)) {
            throw new NullPointerException();
        }
        this.array = array;
        this.index = index;
    }

    public void generate(JFormatter f) {
        f.g(array).p('[').g(index).p(']');
    }

    public JExpression assign(JExpression rhs) {
		return JExpr.assign(this,rhs);
    }
    public JExpression assignPlus(JExpression rhs) {
		return JExpr.assignPlus(this,rhs);
    }
}
