/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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

    public static final String JAXB = "http://java.sun.com/xml/ns/jaxb";

//    public static final QName XSI_NIL = new QName(XML_SCHEMA_INSTANCE,"nil");
}
