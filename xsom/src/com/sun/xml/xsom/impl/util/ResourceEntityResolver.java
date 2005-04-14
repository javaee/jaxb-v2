/*
 * @(#)$Id: ResourceEntityResolver.java,v 1.1 2005-04-14 22:06:33 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class ResourceEntityResolver implements EntityResolver {
    public ResourceEntityResolver( Class _base ) {
        this.base = _base;
    }
    
    private final Class base;
    
    public InputSource resolveEntity( String publicId, String systemId ) {
        return new InputSource(base.getResourceAsStream(systemId));
    }
}
