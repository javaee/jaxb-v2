/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
