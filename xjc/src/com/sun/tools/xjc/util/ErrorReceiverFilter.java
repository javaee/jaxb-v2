/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.util;

import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.api.ErrorListener;

import org.xml.sax.SAXParseException;

/**
 * Filter implementation of the ErrorReceiver.
 * 
 * If an error is encountered, this filter sets a flag.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ErrorReceiverFilter extends ErrorReceiver {

    public ErrorReceiverFilter() {}

    public ErrorReceiverFilter( ErrorListener h ) {
        setErrorReceiver(h);
    }

    private ErrorListener core;
    public void setErrorReceiver( ErrorListener handler ) {
        core = handler;
    }

    private boolean hadError = false;
    public final boolean hadError() { return hadError; }

    public void info(SAXParseException exception) {
        if(core!=null)  core.info(exception);
    }

    public void warning(SAXParseException exception) {
        if(core!=null)  core.warning(exception);
    }

    public void error(SAXParseException exception) {
        hadError = true;
        if(core!=null)  core.error(exception);
    }

    public void fatalError(SAXParseException exception) {
        hadError = true;
        if(core!=null)  core.fatalError(exception);
    }

}
