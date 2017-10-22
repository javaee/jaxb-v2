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
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package com.sun.xml.xsom.parser;

import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParserFactory;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class XSOMParserMultiChameleonTest {

    static final String TNS_ONE = "urn:complex.one";
    static final String TNS_TWO = "urn:complex.two";
    static final String TNS_TOGETHER = "urn:complex.together";

    // TODO specify javax.xml.parsers.SAXParserFactory=[xerces parser factory class name]

    @Test
    public void testRelativeImport() throws Exception {

        final URL resourceChameleon = getClass().getResource("/multi-chameleon/chameleon.xsd");
        final URL resourceOne = getClass().getResource("/multi-chameleon/One.xsd");
        final URL resourceTwo = getClass().getResource("/multi-chameleon/Two.xsd");
        final URL resourceTogether = getClass().getResource("/multi-chameleon/Together.xsd");

        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        // set error handler
        parser.setErrorHandler(new LoggingErrorHandler());

        EntityResolver resolver = mock(EntityResolver.class);
        parser.setEntityResolver(resolver);
        when(resolver.resolveEntity((String) isNull(), eq("urn:common:chameleon.xsd"))).thenAnswer(new InputSourceMockAnswer(resourceChameleon));

        parser.setEntityResolver(resolver);

        // create input sources with resource url as systemId
        parser.parse(new InputSource(resourceTogether.toExternalForm()));

        // parser resolves the import internally
        verify(resolver, times(2)).resolveEntity((String) isNull(), eq("urn:common:chameleon.xsd"));

        // parser context tracks 7 documents of which 1 is an internal one and 3 are the same chameleon schema
        // at different target namespace includes
        assertEquals("Expected 7 schema files parsed", 7, parser.getDocuments().size());

        XSSchemaSet schemaSet = parser.getResult();
        assertEquals("Expected 4 full schemas parsed", 4, schemaSet.getSchemaSize());

        // validate each one
        for (SchemaDocument doc : parser.getDocuments()) {
            XSSchema schema = doc.getSchema();
            if (TNS_ONE.equals(doc.getTargetNamespace())) {
                assertEquals(2, schema.getTypes().size());
                assertEquals(1, schema.getSimpleTypes().size());
                assertNotNull(schema.getSimpleType("SomeCommonType"));
                assertEquals(1, schema.getComplexTypes().size());
                assertNotNull(schema.getComplexType("EntityOne"));
            } else if (TNS_TWO.equals(doc.getTargetNamespace())) {
                // ONE one has 2 types in total
                assertEquals(2, schema.getTypes().size());
                assertEquals(1, schema.getSimpleTypes().size());
                assertNotNull(schema.getSimpleType("SomeCommonType"));
                assertEquals(1, schema.getComplexTypes().size());
                assertNotNull(schema.getComplexType("EntityTwo"));
            } else if (TNS_TOGETHER.equals(doc.getTargetNamespace())) {
                // ONE one has 2 types in total
                assertEquals(2, schema.getTypes().size());
                assertEquals(1, schema.getSimpleTypes().size());
                assertNotNull(schema.getSimpleType("SomeCommonType"));
                assertEquals(1, schema.getComplexTypes().size());
                assertNotNull(schema.getComplexType("Together"));
            }
        }
    }

}
