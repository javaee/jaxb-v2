/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Try statement with Catch and/or Finally clause
 */

public class JTryBlock implements JStatement {

    private JBlock body = new JBlock();
    private List catches = new ArrayList();
    private JBlock _finally = null;

    JTryBlock() { }

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
	for (Iterator i = catches.iterator(); i.hasNext();)
	    f.g((JCatchBlock)(i.next()));
	if (_finally != null)
	    f.p("finally").g(_finally);
	f.nl();
    }

}
