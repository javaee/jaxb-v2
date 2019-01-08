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

package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.SchemaDocument;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Provides context information to be used by {@link NGCCRuntimeEx}s.
 *
 * <p>
 * This class does the actual processing for {@link XSOMParser},
 * but to hide the details from the public API, this class in
 * a different package.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ParserContext {

    /** SchemaSet to which a newly parsed schema is put in. */
    public final SchemaSetImpl schemaSet = new SchemaSetImpl();

    private final XSOMParser owner;

    final XMLParser parser;


    private final Vector<Patch> patchers = new Vector<Patch>();
    private final Vector<Patch> errorCheckers = new Vector<Patch>();

    /**
     * Documents that are parsed already. Used to avoid cyclic inclusion/double
     * inclusion of schemas. Set of {@link SchemaDocumentImpl}s.
     *
     * The actual data structure is map from the canonical format [targetNamespace]|[docuemntId] to the parsed schema.
     */
    private final Map<ParsedKey, SchemaDocumentImpl> parsedDocuments = new HashMap<ParsedKey, SchemaDocumentImpl>();


    public ParserContext( XSOMParser owner, XMLParser parser ) {
        this.owner = owner;
        this.parser = parser;

        try (InputStream is = ParserContext.class.getResourceAsStream("datatypes.xsd")) {
            InputSource source = new InputSource(is);
            source.setSystemId("datatypes.xsd");
            parse(source);

            SchemaImpl xs = (SchemaImpl)
                    schemaSet.getSchema("http://www.w3.org/2001/XMLSchema");
            xs.addSimpleType(schemaSet.anySimpleType,true);
            xs.addComplexType(schemaSet.anyType,true);
        } catch( SAXException | IOException e ) {
            // this must be a bug of XSOM
            throw new InternalError(e.getMessage());
        }
    }

    public EntityResolver getEntityResolver() {
        return owner.getEntityResolver();
    }

    public AnnotationParserFactory getAnnotationParserFactory() {
        return owner.getAnnotationParserFactory();
    }

    /**
     * Parses a new XML Schema document.
     */
    public void parse( InputSource source ) throws SAXException {
        NGCCRuntimeEx runtime = new NGCCRuntimeEx(this);
        runtime.parseEntity(source,false,null,null);
    }

    public boolean hasAlreadyBeenRead(String targetNamespace, String documentId) {
        return parsedDocuments.containsKey(new ParsedKey(targetNamespace, documentId));
    }

    public Set<SchemaDocument> getSchemaDocuments() {
        return Collections.unmodifiableSet(new HashSet<SchemaDocument>(parsedDocuments.values()));
    }

    SchemaDocumentImpl getSchemaDocument(String targetNamespace, String documentId) {
        return parsedDocuments.get(new ParsedKey(targetNamespace, documentId));
    }

    void addSchemaDocument(String targetNamespace, SchemaDocumentImpl document) {
        parsedDocuments.put(new ParsedKey(targetNamespace, document.getSystemId()), document);
    }

    public XSSchemaSet getResult() throws SAXException {
        // run all the patchers
        for (Patch patcher : patchers)
            patcher.run();
        patchers.clear();

        // build the element substitutability map
        Iterator itr = schemaSet.iterateElementDecls();
        while(itr.hasNext())
            ((ElementDecl)itr.next()).updateSubstitutabilityMap();

        // run all the error checkers
        for (Patch patcher : errorCheckers)
            patcher.run();
        errorCheckers.clear();


        if(hadError)    return null;
        else            return schemaSet;
    }


    /** Once an error is detected, this flag is set to true. */
    private boolean hadError = false;

    /** Turns on the error flag. */
    void setErrorFlag() { hadError=true; }

    /**
     * PatchManager implementation, which is accessible only from
     * NGCCRuntimEx.
     */
    final PatcherManager patcherManager = new PatcherManager() {
        public void addPatcher( Patch patch ) {
            patchers.add(patch);
        }
        public void addErrorChecker( Patch patch ) {
            errorCheckers.add(patch);
        }
        public void reportError( String msg, Locator src ) throws SAXException {
            // set a flag to true to avoid returning a corrupted object.
            setErrorFlag();

            SAXParseException e = new SAXParseException(msg,src);
            if(errorHandler==null)
                throw e;
            else
                errorHandler.error(e);
        }
    };

    /**
     * ErrorHandler proxy to turn on the hadError flag when an error
     * is found.
     */
    final ErrorHandler errorHandler = new ErrorHandler() {
        private ErrorHandler getErrorHandler() {
            if( owner.getErrorHandler()==null )
                return noopHandler;
            else
                return owner.getErrorHandler();
        }

        public void warning(SAXParseException e) throws SAXException {
            getErrorHandler().warning(e);
        }

        public void error(SAXParseException e) throws SAXException {
            setErrorFlag();
            getErrorHandler().error(e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            setErrorFlag();
            getErrorHandler().fatalError(e);
        }
    };

    /**
     * {@link ErrorHandler} that does nothing.
     */
    final ErrorHandler noopHandler = new ErrorHandler() {
        public void warning(SAXParseException e) {
        }
        public void error(SAXParseException e) {
        }
        public void fatalError(SAXParseException e) {
            setErrorFlag();
        }
    };

    private final class ParsedKey {

        private String namespace;

        private String documentId;

        public ParsedKey(String namespace, String documentId) {
            this.namespace = namespace;
            this.documentId = documentId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParsedKey)) return false;
            ParsedKey that = (ParsedKey) o;
            return Objects.equals(namespace, that.namespace) &&
                    Objects.equals(documentId, that.documentId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(namespace, documentId);
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Key {")
                    .append(" namespace [ ").append(namespace).append(" ]")
                    .append(", documentId [ ").append(documentId).append(" ] }");
            return sb.toString();
        }

    }

}
