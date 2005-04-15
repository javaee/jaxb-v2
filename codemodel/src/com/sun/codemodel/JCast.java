/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;



/**
 * JMethod invocation
 */

public class JCast extends JExpressionImpl {
    /**
     * JType to which the expression is to be cast.
     */
    private JType type;

    /**
     * JExpression to be cast.
     */
    private JExpression object;

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
