/*
 * @(#)$Id: AnnotationParserFactory.java,v 1.1 2005-04-14 22:06:34 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.parser;

/**
 * Factory for {@link AnnotationParser}.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface AnnotationParserFactory {
    AnnotationParser create();
}

