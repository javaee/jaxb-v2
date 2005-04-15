/*
 * @(#)$Id: Util.java,v 1.1 2005-04-15 20:03:28 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.serializer;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;

import com.sun.xml.bind.util.ValidationEventLocatorExImpl;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Util {
    
    /**
     * Creates a {@link ValidationEvent} object for an error where
     * a JAXB object is missing a required field.
     * 
     * @return always returns non-null valid object.
     */
    public static ValidationEvent createMissingObjectError( Object target, String fieldName ) {
        return new ValidationEventImpl(
            ValidationEvent.ERROR,
            Messages.format(Messages.MISSING_OBJECT,fieldName),
            new ValidationEventLocatorExImpl(target,fieldName),
            new NullPointerException() );
    }
}
