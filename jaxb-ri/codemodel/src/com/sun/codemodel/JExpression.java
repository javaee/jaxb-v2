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
 * A Java expression.
 *
 * <p>
 * Unlike most of CodeModel, JExpressions are built bottom-up (
 * meaning you start from leaves and then gradually build compliated expressions
 * by combining them.)
 *
 * <p>
 * {@link JExpression} defines a series of composer methods,
 * which returns a complicated expression (by often taking other {@link JExpression}s
 * as parameters.
 * For example, you can build "5+2" by
 * <tt>JExpr.lit(5).add(JExpr.lit(2))</tt>
 */
public interface JExpression extends JGenerable {
    /**
     * Returns "-[this]" from "[this]".
     */
    JExpression minus();

    /**
     * Returns "![this]" from "[this]".
     */
    JExpression not();
    /**
     * Returns "~[this]" from "[this]".
     */
    JExpression complement();

    /**
     * Returns "[this]++" from "[this]".
     */
    JExpression incr();

    /**
     * Returns "[this]--" from "[this]".
     */
    JExpression decr();

    /**
     * Returns "[this]+[right]"
     */
    JExpression plus(JExpression right);

    /**
     * Returns "[this]-[right]"
     */
    JExpression minus(JExpression right);

    /**
     * Returns "[this]*[right]"
     */
    JExpression mul(JExpression right);

    /**
     * Returns "[this]/[right]"
     */
    JExpression div(JExpression right);

    /**
     * Returns "[this]%[right]"
     */
    JExpression mod(JExpression right);

    /**
     * Returns "[this]&lt;&lt;[right]"
     */
    JExpression shl(JExpression right);

    /**
     * Returns "[this]>>[right]"
     */
    JExpression shr(JExpression right);

    /**
     * Returns "[this]>>>[right]"
     */
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

    /**
     * Returns "[this] instanceof [right]"
     */
    JExpression _instanceof(JType right);

    /**
     * Returns "[this].[method]".
     *
     * Arguments shall be added to the returned {@link JInvocation} object.
     */
    JInvocation invoke(JMethod method);

    /**
     * Returns "[this].[method]".
     *
     * Arguments shall be added to the returned {@link JInvocation} object.
     */
    JInvocation invoke(String method);
    JFieldRef ref(JVar field);
    JFieldRef ref(String field);
    JArrayCompRef component(JExpression index);
}
