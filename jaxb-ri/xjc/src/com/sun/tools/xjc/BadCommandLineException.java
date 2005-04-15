/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

/**
 * Signals a bad command line argument.
 */
public class BadCommandLineException extends Exception {
    BadCommandLineException(String msg) {
        super(msg);
    }
    BadCommandLineException() {
        this(null);
    }
}