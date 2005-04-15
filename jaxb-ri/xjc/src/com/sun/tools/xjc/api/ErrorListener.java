/*
 * @(#)$Id: ErrorListener.java,v 1.1 2005-04-15 20:08:56 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.api;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Implemented by the driver of the compiler engine to handle
 * errors found during the compiliation.
 * 
 * <p>
 * This class implements {@link ErrorHandler} so it can be
 * passed to anywhere where {@link ErrorHandler} is expected.
 * 
 * <p>
 * However, to make the error handling easy (and make it work
 * with visitor patterns nicely), this interface is not allowed
 * to abort the processing. It merely receives errors.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface ErrorListener extends ErrorHandler {
    void error(SAXParseException exception);
    void fatalError(SAXParseException exception);
    void warning(SAXParseException exception);
    /**
     * Used to report possibly verbose information that
     * can be safely ignored.
     */
    void info(SAXParseException exception);
}
