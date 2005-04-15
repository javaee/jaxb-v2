/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.unmarshaller;


/**
 * Generated classes have to implement this interface for it
 * to be unmarshallable.
 * 
 * @author      Kohsuke KAWAGUCHI
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface UnmarshallableObject
{
    /**
     * Gets an unmarshaller that will unmarshall this object.
     */
    ContentHandlerEx getUnmarshaller( UnmarshallingContext context );
    
    /**
     * Gets the class object for the primary interface
     */
    Class getPrimaryInterfaceClass();
}
