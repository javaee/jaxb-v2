/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * Common interface for code components that can generate
 * uses of themselves as expressions.
 */

public interface JExpression extends JGenerable {
    JExpression minus();
    JExpression not();
    JExpression complement();
    JExpression incr();
    JExpression decr();
    JExpression plus(JExpression right);
    JExpression minus(JExpression right);
    JExpression mul(JExpression right);
    JExpression div(JExpression right);
    JExpression mod(JExpression right);
    JExpression shl(JExpression right);
    JExpression shr(JExpression right);
    JExpression shrz(JExpression right);
    /** Bit-wise AND '&amp;'. */
    JExpression band(JExpression right);
    /** Bit-wise OR '|'. */
    JExpression bor(JExpression right);
    /** Logical AND '&amp;&amp;'. */
    JExpression cand(JExpression right);
    /** Logical OR '||'. */
    JExpression cor(JExpression right);
    JExpression xor(JExpression right);
    JExpression lt(JExpression right);
    JExpression lte(JExpression right);
    JExpression gt(JExpression right);
    JExpression gte(JExpression right);
    JExpression eq(JExpression right);
    JExpression ne(JExpression right);
    JExpression _instanceof(JType right);

    JInvocation invoke(JMethod method);
    JInvocation invoke(String method);
    JFieldRef ref(JVar field);
    JFieldRef ref(String field);
    JArrayCompRef component(JExpression index);
}
