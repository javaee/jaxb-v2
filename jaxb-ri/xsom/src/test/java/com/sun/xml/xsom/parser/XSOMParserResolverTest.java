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

import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class XSOMParserResolverTest {

    static final String TNS_ONE = "urn:complex.one";
    static final String TNS_TWO = "urn:complex.two";

    // TODO specify javax.xml.parsers.SAXParserFactory=[xerces parser factory class name]

    @Test
    public void testRelativeImport() throws Exception {

        final URL resourceOne = getClass().getResource("/resolver/One.xsd");
        final URL resourceTwo = getClass().getResource("/resolver/TwoRelativeImport.xsd");

        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        // set error handler
        parser.setErrorHandler(new LoggingErrorHandler());

        EntityResolver resolver = mock(EntityResolver.class);
        parser.setEntityResolver(resolver);

        // create input sources with resource url as systemId
        parser.parse(new InputSource(resourceOne.toExternalForm()));
        validateSchemaSetOneOnly(parser.getDocuments());

        parser.parse(new InputSource(resourceTwo.toExternalForm()));
        validateSchemaSetImports(parser.getDocuments());

        // parser resolves the import internally
        verify(resolver, never()).resolveEntity((String) isNull(), anyString());

    }

    @Test
    public void testResolverImport() throws Exception {

        final URL resourceOne = getClass().getResource("/resolver/One.xsd");
        final URL resourceTwo = getClass().getResource("/resolver/TwoResolverImport.xsd");

        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        // set error handler
        parser.setErrorHandler(new LoggingErrorHandler());

        EntityResolver resolver = mock(EntityResolver.class);
        parser.setEntityResolver(resolver);
        when(resolver.resolveEntity(eq(TNS_ONE), eq(TNS_ONE + ":One.xsd"))).thenAnswer(new InputSourceMockAnswer(resourceOne));

        // create input sources with resource url as systemId
        parser.parse(new InputSource(resourceOne.toExternalForm()));
        validateSchemaSetOneOnly(parser.getDocuments());

        parser.parse(new InputSource(resourceTwo.toExternalForm()));
        validateSchemaSetImports(parser.getDocuments());

        // parser would have had to delegate to the entity resolver to get an answer for urn:complex.one:One.xsd
        verify(resolver, times(1)).resolveEntity(eq(TNS_ONE), eq(TNS_ONE + ":One.xsd"));

    }

    @Test
    public void testRelativeInclude() throws Exception {

        final URL resourceOne = getClass().getResource("/resolver/One.xsd");
        final URL resourceTwo = getClass().getResource("/resolver/TwoRelativeInclude.xsd");

        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        // set error handler
        parser.setErrorHandler(new LoggingErrorHandler());

        EntityResolver resolver = mock(EntityResolver.class);
        parser.setEntityResolver(resolver);

        // the first include is that using the systemId of resource One.xsd
        when(resolver.resolveEntity((String) isNull(), eq(resourceOne.toExternalForm()))).thenAnswer(new InputSourceMockAnswer());

        // create input sources with resource url as systemId
        parser.parse(new InputSource(resourceOne.toExternalForm()));
        validateSchemaSetOneOnly(parser.getDocuments());

        parser.parse(new InputSource(resourceTwo.toExternalForm()));
        validateSchemaSetIncludes(parser.getDocuments());

        // parser resolves the import internally
        verify(resolver, times(1)).resolveEntity((String) isNull(), eq(resourceOne.toExternalForm()));

    }

    @Test
    public void testResolverInclude() throws Exception {

        final URL resourceOne = getClass().getResource("/resolver/One.xsd");
        final URL resourceTwo = getClass().getResource("/resolver/TwoResolverInclude.xsd");

        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        // set error handler
        parser.setErrorHandler(new LoggingErrorHandler());

        EntityResolver resolver = mock(EntityResolver.class);
        parser.setEntityResolver(resolver);

        // the first include is that using the systemId of resource One.xsd
        when(resolver.resolveEntity((String) isNull(), eq(TNS_ONE + ":One.xsd"))).thenAnswer(new InputSourceMockAnswer());

        // create input sources with resource url as systemId
        parser.parse(new InputSource(resourceOne.toExternalForm()));
        validateSchemaSetOneOnly(parser.getDocuments());

        parser.parse(new InputSource(resourceTwo.toExternalForm()));
        validateSchemaSetIncludes(parser.getDocuments());

        // parser will first try to do a publicId lookup
        verify(resolver, times(1)).resolveEntity(eq(TNS_ONE + ":One.xsd"), (String) isNull());

        // parser then resolves with schemaLocation
        verify(resolver, times(1)).resolveEntity((String) isNull(), eq(TNS_ONE + ":One.xsd"));

    }

    @Test
    public void testResolverIncludePublicId() throws Exception {

        final URL resourceOne = getClass().getResource("/resolver/One.xsd");
        final URL resourceTwo = getClass().getResource("/resolver/TwoResolverInclude.xsd");

        XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
        // set error handler
        parser.setErrorHandler(new LoggingErrorHandler());

        EntityResolver resolver = mock(EntityResolver.class);
        parser.setEntityResolver(resolver);

        // the first include is that using the systemId of resource One.xsd
        when(resolver.resolveEntity(eq(TNS_ONE + ":One.xsd"), (String) isNull())).thenAnswer(new InputSourceMockAnswer(TNS_ONE + ":One.xsd", resourceOne));

        // create input sources with resource url as systemId
        parser.parse(new InputSource(resourceOne.toExternalForm()));
        validateSchemaSetOneOnly(parser.getDocuments());

        parser.parse(new InputSource(resourceTwo.toExternalForm()));
        validateSchemaSetIncludes(parser.getDocuments());

        // parser resolves the import internally
        verify(resolver, times(1)).resolveEntity(eq(TNS_ONE + ":One.xsd"), (String) isNull());

    }


    private void validateSchemaSetOneOnly(Set<SchemaDocument> documents) {
        assertEquals(2, documents.size());
        boolean foundOneSchema = false;
        for (SchemaDocument schema : documents) {
            if (TNS_ONE.equals(schema.getSchema().getTargetNamespace())) {
                validateSchemaOne(schema);
                foundOneSchema = true;
            }
        }
        assertTrue("schema set for One.xsd was not found in parser", foundOneSchema);
    }

    private void validateSchemaSetImports(Set<SchemaDocument> documents) {
        assertEquals(3, documents.size());
        boolean foundOneSchema = false;
        boolean foundTwoSchema = false;
        for (SchemaDocument schema : documents) {
            if (TNS_ONE.equals(schema.getSchema().getTargetNamespace())) {
                validateSchemaOne(schema);
                foundOneSchema = true;
            } else if (TNS_TWO.equals(schema.getSchema().getTargetNamespace())) {
                validateSchemaTwo(schema);
                foundTwoSchema = true;
            }
        }
        assertTrue("schema set for One.xsd was not found in parser", foundOneSchema);
        assertTrue("schema set for Two.xsd was not found in parser", foundTwoSchema);
    }

    private void validateSchemaSetIncludes(Set<SchemaDocument> documents) {
        assertEquals(3, documents.size());
        boolean foundOneSchema = false;
        for (SchemaDocument schema : documents) {
            if (TNS_ONE.equals(schema.getSchema().getTargetNamespace())) {
                validateSchemaIncludes(schema);
                foundOneSchema = true;
            }
        }
        assertTrue("schema set for One.xsd was not found in parser", foundOneSchema);
    }

    private void validateSchemaOne(SchemaDocument schema) {
        assertEquals("Expecting only 2 type definitions", 2, schema.getSchema().getTypes().size());
        assertNotNull(schema.getSchema().getSimpleType("SomeReusableOne"));
        assertNotNull(schema.getSchema().getComplexType("EntityOne"));
        assertEquals("Expecting only 1 entity definitions", 1, schema.getSchema().getElementDecls().size());
        assertNotNull(schema.getSchema().getElementDecl("EntityOne"));
    }

    private void validateSchemaTwo(SchemaDocument schema) {
        assertEquals("Expecting only 2 type definitions", 2, schema.getSchema().getTypes().size());
        assertNotNull(schema.getSchema().getSimpleType("SomeReusableTwo"));
        assertNotNull(schema.getSchema().getComplexType("EntityTwo"));
        assertEquals("Expecting only 1 entity definitions", 1, schema.getSchema().getElementDecls().size());
        assertNotNull(schema.getSchema().getElementDecl("EntityTwo"));
    }

    private void validateSchemaIncludes(SchemaDocument schema) {
        assertEquals("Expecting only 4 type definitions", 4, schema.getSchema().getTypes().size());
        assertNotNull(schema.getSchema().getSimpleType("SomeReusableOne"));
        assertNotNull(schema.getSchema().getSimpleType("SomeReusableTwo"));
        assertNotNull(schema.getSchema().getComplexType("EntityOne"));
        assertNotNull(schema.getSchema().getComplexType("EntityTwo"));
        assertEquals("Expecting only 2 entity definitions", 2, schema.getSchema().getElementDecls().size());
        assertNotNull(schema.getSchema().getElementDecl("EntityOne"));
        assertNotNull(schema.getSchema().getElementDecl("EntityTwo"));
    }

}
