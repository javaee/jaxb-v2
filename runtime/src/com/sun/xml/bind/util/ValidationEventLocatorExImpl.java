/*
 * @(#)$Id: ValidationEventLocatorExImpl.java,v 1.1 2005-04-15 20:03:50 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.util;

import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import com.sun.xml.bind.ValidationEventLocatorEx;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ValidationEventLocatorExImpl
    extends ValidationEventLocatorImpl implements ValidationEventLocatorEx {
    
    private final String fieldName;
        
    public ValidationEventLocatorExImpl( Object target, String fieldName ) {
        super(target);
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Returns a nice string representation for better debug experience.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[url=");
        buf.append(getURL());
        buf.append(",line=");
        buf.append(getLineNumber());
        buf.append(",column=");
        buf.append(getColumnNumber());
        buf.append(",node=");
        buf.append(getNode());
        buf.append(",object=");
        buf.append(getObject());
        buf.append(",field=");
        buf.append(getFieldName());
        buf.append("]");
        
        return buf.toString();
    }
}
