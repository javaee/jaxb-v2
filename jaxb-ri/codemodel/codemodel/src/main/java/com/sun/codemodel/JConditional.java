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
 * If statement, with optional else clause
 */

public class JConditional implements JStatement {

    /**
     * JExpression to test to determine branching
     */
    private JExpression test = null;

    /**
     * JBlock of statements for "then" clause
     */
    private JBlock _then = new JBlock();

    /**
     * JBlock of statements for optional "else" clause
     */
    private JBlock _else = null;

    /**
     * Constructor
     *
     * @param test
     *        JExpression which will determine branching
     */
    JConditional(JExpression test) {
       this.test = test;
    }

    /**
     * Return the block to be excuted by the "then" branch
     *
     * @return Then block
     */
    public JBlock _then() {
        return _then;
    }

    /**
     * Create a block to be executed by "else" branch
     *
     * @return Newly generated else block
     */
    public JBlock _else() {
        if (_else == null) _else = new JBlock();
        return _else;
    }

    /**
     * Creates <tt>... else if(...) ...</tt> code.
     */
    public JConditional _elseif(JExpression boolExp) {
        return _else()._if(boolExp);
    }

    public void state(JFormatter f) {
        if(test==JExpr.TRUE) {
            _then.generateBody(f);
            return;
        }
        if(test==JExpr.FALSE) {
            _else.generateBody(f);
            return;
        }

        if (JOp.hasTopOp(test)) {
            f.p("if ").g(test);
        } else {
            f.p("if (").g(test).p(')');
        }
        f.g(_then);
        if (_else != null)
            f.p("else").g(_else);
        f.nl();
    }
}
