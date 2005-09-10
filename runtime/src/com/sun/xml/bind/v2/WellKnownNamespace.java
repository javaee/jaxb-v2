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
