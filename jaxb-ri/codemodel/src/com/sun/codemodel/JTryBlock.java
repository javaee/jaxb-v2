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

import java.util.ArrayList;
import java.util.List;


/**
 * Try statement with Catch and/or Finally clause
 */

public class JTryBlock implements JStatement {

    private JBlock body = new JBlock();
    private List<JCatchBlock> catches = new ArrayList<JCatchBlock>();
    private JBlock _finally = null;

    JTryBlock() {
    }

    public JBlock body() {
        return body;
    }

    public JCatchBlock _catch(JClass exception) {
        JCatchBlock cb = new JCatchBlock(exception);
        catches.add(cb);
        return cb;
    }

    public JBlock _finally() {
        if (_finally == null) _finally = new JBlock();
        return _finally;
    }

    public void state(JFormatter f) {
        f.p("try").g(body);
        for (JCatchBlock cb : catches)
            f.g(cb);
        if (_finally != null)
            f.p("finally").g(_finally);
        f.nl();
    }

}
