/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * A return statement
 */
class JReturn implements JStatement {

    /**
     * JExpression to return; may be null.
     */
    private JExpression expr;

    /**
     * JReturn constructor
     *
     * @param expr
     *        JExpression which evaluates to return value
     */
    JReturn(JExpression expr) {
       this.expr = expr;
    }

    public void state(JFormatter f) {
        f.p("return ");
        if (expr != null) f.g(expr);
        f.p(';').nl();
    }

}
