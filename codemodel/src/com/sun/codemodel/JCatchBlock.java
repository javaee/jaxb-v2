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
 * Catch block for a try/catch/finally statement
 */

public class JCatchBlock implements JGenerable {

    JClass exception;
    private JVar var = null;
    private JBlock body = new JBlock();

    JCatchBlock(JClass exception) {
        this.exception = exception;
    }

    public JVar param(String name) {
        if (var != null) throw new IllegalStateException();
        var = new JVar(JMods.forVar(JMod.NONE), exception, name, null);
        return var;
    }

    public JBlock body() {
        return body;
    }

    public void generate(JFormatter f) {
        if (var == null)
            var = new JVar(JMods.forVar(JMod.NONE),
                    exception, "_x", null);
        f.p("catch (").b(var).p(')').g(body);
    }

}
