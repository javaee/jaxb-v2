/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JBlock;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ExistingBlockReference implements BlockReference {
    private final JBlock block;
    
    public ExistingBlockReference( JBlock _block ) {
        this.block = _block;
    }
    
    public JBlock get(boolean create) {
        return block;
    }

}
