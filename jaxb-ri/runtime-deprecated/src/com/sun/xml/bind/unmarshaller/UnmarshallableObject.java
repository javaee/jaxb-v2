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
