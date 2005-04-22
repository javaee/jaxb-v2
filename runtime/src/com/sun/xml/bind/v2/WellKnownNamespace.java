/*
 * @(#)$Id: WellKnownNamespace.java,v 1.2 2005-04-22 21:26:39 kohsuke Exp $
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

    public static final String XML_NAMESPACE_URI =
        "http://www.w3.org/XML/1998/namespace";

    public static final String XOP =
        "http://www.w3.org/2004/08/xop/include";
    
    public static final String SWA_URI =
        "http://ws-i.org/profiles/basic/1.1/xsd";
    
    /**
     * TODO: be sure to update this URI.
     *
     * http://dev.w3.org/cvsweb/~checkout~/2002/ws/desc/media-types/xml-media-types.html?content-type=text/html;%20charset=utf-8#static
     */
    public static final String XML_MIME_URI = "http://www.w3.org/@@@@/@@/xmlmime";
            

//    public static final QName XSI_NIL = new QName(XML_SCHEMA_INSTANCE,"nil");
}
