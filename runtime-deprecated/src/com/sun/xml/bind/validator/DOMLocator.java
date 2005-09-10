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
package com.sun.xml.bind.validator;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import com.sun.xml.bind.unmarshaller.DOMScanner;

import org.xml.sax.SAXParseException;


/**
 * @since JAXB1.0
 */
public class DOMLocator implements Locator {
    
    private DOMScanner scanner = null;
    
    public DOMLocator( DOMScanner scanner ) {
        this.scanner = scanner;
    }
    
    /**
     * Set the appropriate locator information on the supplied
     * ValidationEvent.
     */
    public ValidationEventLocator getLocation( SAXParseException saxException ) {
                                                   
        ValidationEventLocatorImpl vel = new ValidationEventLocatorImpl();
        
        try {
            vel.setURL( new URL( saxException.getSystemId() ) );
        } catch( MalformedURLException me ) {
            vel.setURL( null );
        }
        // get the location from the scanner and set it on the locator
        vel.setNode( scanner.getCurrentLocation() );
        return vel;
    }

    public ValidationEventLocator getLocation(org.xml.sax.Locator location) {
        ValidationEventLocatorImpl vel = new ValidationEventLocatorImpl();

        try {
            vel.setURL( new URL( location.getSystemId() ) );
        } catch( MalformedURLException me ) {
            vel.setURL( null );
        }
        // get the location from the scanner and set it on the locator
        vel.setNode( scanner.getCurrentLocation() );
        return vel;
    }

}
