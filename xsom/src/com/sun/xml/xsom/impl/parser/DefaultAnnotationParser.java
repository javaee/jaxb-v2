/*
 * @(#)$Id: DefaultAnnotationParser.java,v 1.1 2005-04-14 22:06:29 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.parser;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.xsom.parser.*;

/**
 * AnnotationParser that just ignores annotation.
 * 
 * <p>
 * This class doesn't have any state. So it should be used as a singleton.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class DefaultAnnotationParser extends AnnotationParser {
    
    private DefaultAnnotationParser() {}
    
    public static final AnnotationParser theInstance = new DefaultAnnotationParser();
    
    public ContentHandler getContentHandler(
        AnnotationContext contest, String elementName,
        ErrorHandler errorHandler, EntityResolver entityResolver ) {
        return new DefaultHandler();
    }
    
    public Object getResult( Object existing ) {
        return null;
    }
    
    
}

