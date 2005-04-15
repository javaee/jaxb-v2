/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
