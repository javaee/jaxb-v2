/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.webapp.tabpage;

/**
 * Enum constants.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class Pass {
    
    private Pass() {}
    
    public static final Pass header = new Pass();
    public static final Pass body = new Pass();
}
