/*
 * @(#)$Id: XSAnnotation.java,v 1.1 2005-04-14 22:06:18 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

import org.xml.sax.Locator;
import com.sun.xml.xsom.parser.AnnotationParser;

/**
 * <a href="http://www.w3.org/TR/xmlschema-1/#Annotation_details">
 * XML Schema annotation</a>.
 * 
 * 
 */
public interface XSAnnotation
{
    /**
     * Obtains the application-parsed annotation.
     * <p>
     * annotations are parsed by the user-specified
     * {@link AnnotationParser}.
     * 
     * @return may return null
     */
    Object getAnnotation();

    /**
     * Returns a location information of the annotation.
     */
    Locator getLocator();
}
