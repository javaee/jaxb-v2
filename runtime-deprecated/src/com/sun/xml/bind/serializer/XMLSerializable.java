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
 * For a generated class to be serializable, it has to
 * implement this interface.
 * 
 * @author Kohsuke Kawaguchi
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface XMLSerializable
{
    /**
     * Serializes child elements and texts into the specified target.
     */
    void serializeElements( XMLSerializer target ) throws SAXException;
    /**
     * Serializes attributes into the specified target.
     */
    void serializeAttributes( XMLSerializer target ) throws SAXException;
    /**
     * Serializes texts as values of attributes into the specified target.
     */
    void serializeAttributeBodies( XMLSerializer target ) throws SAXException;
}
