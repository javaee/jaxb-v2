/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.validator;

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

/**
 * @since JAXB1.0
 */
public class SAXLocator implements Locator {
    
    public static final SAXLocator theInstance = new SAXLocator();
    
    /**
     * Set the appropriate locator information on the supplied
     * ValidationEvent.
     */
    public ValidationEventLocator getLocation( SAXParseException saxException ) {
        return new ValidationEventLocatorImpl(saxException);
    }

    public ValidationEventLocator getLocation(org.xml.sax.Locator location) {
        // copy to take the snapshot of the current values 
        return new ValidationEventLocatorImpl(new LocatorImpl(location));
    }
}
