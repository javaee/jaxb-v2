/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
