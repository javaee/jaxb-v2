/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

/**
 * Anonymous class quick hack.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class JAnonymousClass extends JDefinedClass {

    /**
     * Base interface/class from which this anonymous class is built.
     */
    private final JClass base;
    
    JAnonymousClass( JClass _base) {
        super(_base.owner(), 0, null);
        this.base = _base;
    }
    
    public String fullName() {
    	return base.fullName();
    }
}
