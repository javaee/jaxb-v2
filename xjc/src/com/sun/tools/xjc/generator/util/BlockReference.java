/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.util;

import com.sun.codemodel.JBlock;

/**
 * Holds a reference to a {@link JBlock} object.
 * 
 * <p>
 * This interface is usually used when one wants to create
 * a new JBlock object lazily.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface BlockReference {
    /**
     * @param create
     *      If false, the method will return null if the block is
     *      not yet created.  
     */
    JBlock get(boolean create);
}
