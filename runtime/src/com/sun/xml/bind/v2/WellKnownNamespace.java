/*
 * @(#)$Id: WellKnownNamespace.java,v 1.4 2005-05-25 21:39:12 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.bind.v2;

/**
 * Well-known namespace URIs.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since 2.0
 */
public abstract class WellKnownNamespace {
    private WellKnownNamespace() {} // no instanciation please

    public static final String XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    public static final String XML_SCHEMA_INSTANCE =
        "http://www.w3.org/2001/XMLSchema-instance";

    public static final Object XML_SCHEMA_DATATYPES =
        "http://www.w3.org/2001/XMLSchema-datatypes";

    public static final String XML_NAMESPACE_URI =
        "http://www.w3.org/XML/1998/namespace";

    public static final String XOP =
        "http://www.w3.org/2004/08/xop/include";
    
    public static final String SWA_URI =
        "http://ws-i.org/profiles/basic/1.1/xsd";
    
    public static final String XML_MIME_URI = "http://www.w3.org/2005/05/xmlmime";
            

//    public static final QName XSI_NIL = new QName(XML_SCHEMA_INSTANCE,"nil");
}
