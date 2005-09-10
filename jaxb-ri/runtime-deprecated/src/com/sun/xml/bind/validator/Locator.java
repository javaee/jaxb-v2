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
