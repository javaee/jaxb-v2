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
 * Assignment statements, which are also expressions.
 */
public class JAssignment extends JExpressionImpl implements JStatement {

    JAssignmentTarget lhs;
    JExpression rhs;
    String op = "";

    JAssignment(JAssignmentTarget lhs, JExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    JAssignment(JAssignmentTarget lhs, JExpression rhs, String op) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    public void generate(JFormatter f) {
        f.g(lhs).p(op + '=').g(rhs);
    }

    public void state(JFormatter f) {
        f.g(this).p(';').nl();
    }

}
