/*
 * @(#)$Id: DraconianErrorHandler.java,v 1.1 2005-04-14 22:06:32 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Aborts on the first error.
 */
public class DraconianErrorHandler implements ErrorHandler {
    public void error( SAXParseException e ) throws SAXException {
        throw e;
    }
    public void fatalError( SAXParseException e ) throws SAXException {
        throw e;
    }
    public void warning( SAXParseException e ) {}
}
