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
