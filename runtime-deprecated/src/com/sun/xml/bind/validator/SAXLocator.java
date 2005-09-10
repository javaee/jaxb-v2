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
