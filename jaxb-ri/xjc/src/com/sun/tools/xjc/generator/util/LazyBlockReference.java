/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JBlock;

/**
 * Lazy block reference.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class LazyBlockReference implements BlockReference {
    
    private JBlock block = null;
    
    /**
     * Called when a block needs to be created.
     * Only called once in the whole life time of this object.
     */
    protected abstract JBlock create();
    
    public JBlock get(boolean create) {
        if(!create)     return block;
        if(block==null)
            block = create();
        return block;
    }

}
