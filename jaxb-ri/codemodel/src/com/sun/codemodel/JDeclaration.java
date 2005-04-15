/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * Common interface for code components that can generate declarations
 * of themselves.
 */

public interface JDeclaration {

    public void declare(JFormatter f);

}
