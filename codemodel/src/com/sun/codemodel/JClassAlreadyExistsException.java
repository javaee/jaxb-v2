/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

/**
 * Indicates that the class is already created.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JClassAlreadyExistsException extends Exception {
    private final JDefinedClass existing;
    
    public JClassAlreadyExistsException( JDefinedClass _existing ) {
        this.existing = _existing;
    }
    
    /**
     * Gets a reference to the existing {@link JDefinedClass}.
     * 
     * @return
     *      This method always return non-null valid object.
     */
    public JDefinedClass getExistingClass() {
        return existing;
    }
}
