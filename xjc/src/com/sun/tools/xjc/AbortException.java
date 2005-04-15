/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc;


/**
 * Signals the abortion of the compilation.
 * <p>
 * This exception should be only thrown from {@link ErrorReceiver}
 * for the consistent error handling.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AbortException extends RuntimeException {
    public AbortException() {
    }
}
    
