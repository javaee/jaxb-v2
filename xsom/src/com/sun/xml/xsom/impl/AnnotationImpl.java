/*
 * @(#)$Id: AnnotationImpl.java,v 1.1 2005-04-14 22:06:23 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSAnnotation;

public class AnnotationImpl implements XSAnnotation
{
    private final Object annotation;
    public Object getAnnotation() { return annotation; }
    
    private final Locator locator;
    public Locator getLocator() { return locator; }
    
    public AnnotationImpl( Object o, Locator _loc ) {
        this.annotation = o;
        this.locator = _loc;
    }
}
