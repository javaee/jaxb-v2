/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This class is responsible for producing RI JAXBContext objects.  In
 * the RI, this is the class that the javax.xml.bind.context.factory 
 * property will point to.
 *
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class ContextFactory {

    public static JAXBContext createContext( String contextPath, 
                                             ClassLoader classLoader ) 
        throws JAXBException {
            
        return new DefaultJAXBContextImpl( contextPath, classLoader );
    }
    
}
