/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.webapp;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Prohibits entity resolution to resolve to non-HTTP resources.
 * Useful when we are allowing user to submit XMLs.
 * 
 * Those XML can refer to server local resource, and that could
 * introduce a security risk.
 * 
 * This entity resolver avoids that by just allowing http resources.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SecureEntityResolver implements EntityResolver {

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if( systemId==null )    return null;    // let the default mechanism handle this
        
        String id = systemId.toLowerCase();
        if( isSafe(id) )
            return null;    // OK
        else
            return new InputSource(new StringReader("processing of "+systemId+" rejected by a security reason"));
    }

    private boolean isSafe(String id) {
        return id.startsWith("http:") || id.startsWith("https:") || id.startsWith("ftp:");
    }

}
