/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

/**
 * Signals a bad command line argument.
 */
public class BadCommandLineException extends Exception {
    public BadCommandLineException(String msg) {
        super(msg);
    }
    public BadCommandLineException() {
        this(null);
    }
}