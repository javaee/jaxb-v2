/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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