/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * JAtoms: Simple code components that merely generate themselves.
 */

class JAtom extends JExpressionImpl {
    
    String what;
    
    JAtom(String what) {
        this.what = what;
    }
    
    public void generate(JFormatter f) {
        f.p(what);
    }
}
