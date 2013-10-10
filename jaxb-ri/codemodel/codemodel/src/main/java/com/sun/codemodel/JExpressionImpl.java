/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.codemodel;

/**
 * Provides default implementations for {@link JExpression}.
 */
public abstract class JExpressionImpl implements JExpression
{
    //
    //
    // from JOp
    //
    //
    public final JExpression minus() {
        return JOp.minus(this);
    }

    /**
     * Logical not <tt>'!x'</tt>.
     */
    public final JExpression not() {
        return JOp.not(this);
    }

    public final JExpression complement() {
        return JOp.complement(this);
    }

    public final JExpression incr() {
        return JOp.incr(this);
    }

    public final JExpression decr() {
        return JOp.decr(this);
    }

    public final JExpression plus(JExpression right) {
        return JOp.plus(this, right);
    }

    public final JExpression minus(JExpression right) {
        return JOp.minus(this, right);
    }

    public final JExpression mul(JExpression right) {
        return JOp.mul(this, right);
    }

    public final JExpression div(JExpression right) {
        return JOp.div(this, right);
    }

    public final JExpression mod(JExpression right) {
        return JOp.mod(this, right);
    }

    public final JExpression shl(JExpression right) {
        return JOp.shl(this, right);
    }

    public final JExpression shr(JExpression right) {
        return JOp.shr(this, right);
    }

    public final JExpression shrz(JExpression right) {
        return JOp.shrz(this, right);
    }

    public final JExpression band(JExpression right) {
        return JOp.band(this, right);
    }

    public final JExpression bor(JExpression right) {
        return JOp.bor(this, right);
    }

    public final JExpression cand(JExpression right) {
        return JOp.cand(this, right);
    }

    public final JExpression cor(JExpression right) {
        return JOp.cor(this, right);
    }

    public final JExpression xor(JExpression right) {
        return JOp.xor(this, right);
    }

    public final JExpression lt(JExpression right) {
        return JOp.lt(this, right);
    }

    public final JExpression lte(JExpression right) {
        return JOp.lte(this, right);
    }

    public final JExpression gt(JExpression right) {
        return JOp.gt(this, right);
    }

    public final JExpression gte(JExpression right) {
        return JOp.gte(this, right);
    }

    public final JExpression eq(JExpression right) {
        return JOp.eq(this, right);
    }

    public final JExpression ne(JExpression right) {
        return JOp.ne(this, right);
    }

    public final JExpression _instanceof(JType right) {
        return JOp._instanceof(this, right);
    }

    //
    //
    // from JExpr
    //
    //
    public final JInvocation invoke(JMethod method) {
        return JExpr.invoke(this, method);
    }

    public final JInvocation invoke(String method) {
        return JExpr.invoke(this, method);
    }

    public final JFieldRef ref(JVar field) {
        return JExpr.ref(this, field);
    }

    public final JFieldRef ref(String field) {
        return JExpr.ref(this, field);
    }

    public final JArrayCompRef component(JExpression index) {
        return JExpr.component(this, index);
    }
}
