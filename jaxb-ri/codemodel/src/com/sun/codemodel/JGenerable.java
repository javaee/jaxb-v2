/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;


/**
 * Common interface for code components that can generate
 * uses of themselves.
 */

interface JGenerable {

    public void generate(JFormatter f);

}
