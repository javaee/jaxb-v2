/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.bind.serializer;

/**
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public interface PrefixCallback
{
    void onPrefixMapping( String prefix, String uri ) throws org.xml.sax.SAXException;
}
