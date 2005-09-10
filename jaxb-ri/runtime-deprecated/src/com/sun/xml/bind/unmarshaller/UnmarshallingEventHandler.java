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

import org.xml.sax.Attributes;

/**
 * Implemented by the generated code to unmarshall an object
 * from unmarshaller events.
 * 
 * <p>
 * ContentHandlerEx throws a SAXException when a problem is encountered
 * and that problem is not reported. It is the responsibility of the caller
 * of this interface to report it to the client's ValidationEventHandler
 * and re-wrap it into UnmarshalException.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface UnmarshallingEventHandler {
    void enterElement(String uri, String local, Attributes atts) throws UnreportedException;
    void leaveElement(String uri, String local) throws UnreportedException;
    void text(String s) throws UnreportedException;
    void enterAttribute(String uri, String local) throws UnreportedException;
    void leaveAttribute(String uri, String local) throws UnreportedException;
    void leaveChild(int nextState) throws UnreportedException;
}