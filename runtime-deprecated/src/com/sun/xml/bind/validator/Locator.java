/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.validator;

import javax.xml.bind.ValidationEventLocator;

import org.xml.sax.SAXParseException;

/**
 * Encapsulates how the location information is obtained.
 *
 * <p>
 * The "get" methods are really more like the "create" methods,
 * but unfortunately we can't rename them because of the backward
 * compatibility.
 *
 * <p>
 * This interface is not implemented by the generated code,
 * so new methods can be added.
 *
 * @since JAXB1.0
 */
public interface Locator {
    
    /**
     * Get the appropriate locator information for the given error. 
     */
    ValidationEventLocator getLocation( SAXParseException saxException );

    /**
     * @since 2.0
     */
    ValidationEventLocator getLocation( org.xml.sax.Locator location );
}
