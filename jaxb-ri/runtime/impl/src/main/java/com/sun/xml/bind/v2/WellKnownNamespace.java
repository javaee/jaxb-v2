/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import javax.xml.XMLConstants;

/**
 * Well-known namespace URIs.
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com), Martin Grebac (martin.grebac@oracle.com)
 * @since 2.0
 */
public abstract class WellKnownNamespace {
    private WellKnownNamespace() {} // no instanciation please

    /**
     * @deprecated Use javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI instead;
     */
    @Deprecated()
    public static final String XML_SCHEMA = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    /**
     * @deprecated Use javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI instead
     */
    @Deprecated()
    public static final String XML_SCHEMA_INSTANCE = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

    /**
     * @deprecated Use javax.xml.XMLConstants.XML_NS_URI instead;
     */
    @Deprecated()
    public static final String XML_NAMESPACE_URI = XMLConstants.XML_NS_URI;

    public static final String XOP = "http://www.w3.org/2004/08/xop/include";

    public static final String SWA_URI = "http://ws-i.org/profiles/basic/1.1/xsd";

    public static final String XML_MIME_URI = "http://www.w3.org/2005/05/xmlmime";

    public static final String JAXB = "http://java.sun.com/xml/ns/jaxb";

}
