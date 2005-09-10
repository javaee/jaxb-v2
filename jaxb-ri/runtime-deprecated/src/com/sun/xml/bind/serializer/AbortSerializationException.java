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

package com.sun.xml.bind.serializer;

import org.xml.sax.SAXException;

/**
 * Thrown to call off the serialization operation.
 * 
 * This exception derives SAXException since
 * many interfaces we internally use are based on SAX
 * and it's easy to propage SAXException.
 * 
 * @since JAXB1.0
 */
public class AbortSerializationException extends SAXException
{
    public AbortSerializationException(Exception e) {
        super(e);
    }
    public AbortSerializationException(String s) {
        super(s);
    }
}
