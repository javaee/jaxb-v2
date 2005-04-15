/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.unmarshaller;

import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEventHandler;

/**
 * Unified event handler that processes
 * both the SAX events and error events.
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface SAXUnmarshallerHandler extends UnmarshallerHandler, ValidationEventHandler {

}
