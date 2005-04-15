/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * JThrow statement
 */

class JThrow implements JStatement {

    /**
     * JExpression to throw
     */
    private JExpression expr;

    /**
     * JThrow constructor
     *
     * @param expr
     *        JExpression which evaluates to JThrow value
     */
    JThrow(JExpression expr) {
       this.expr = expr;
    }

    public void state(JFormatter f) {
        f.p("throw");
        f.g(expr);
        f.p(';').nl();
    }

}
