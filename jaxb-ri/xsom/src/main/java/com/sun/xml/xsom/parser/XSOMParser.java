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

package com.sun.xml.xsom.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.parser.NGCCRuntimeEx;
import com.sun.xml.xsom.impl.parser.ParserContext;
import com.sun.xml.xsom.impl.parser.state.Schema;

/**
 * Parses possibly multiple W3C XML Schema files and compose
 * them into one grammar.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class XSOMParser {

    private EntityResolver entityResolver;
    private ErrorHandler userErrorHandler;
    
    private AnnotationParserFactory apFactory;
    
    private final ParserContext context;
    
    /**
    * Creates a new XSOMParser by using a SAX parser from JAXP.
     * @deprecated Unsafe, use XSOMParser(factory) instead with 
     * security features initialized by setting 
     * XMLConstants.FEATURE_SECURE_PROCESSING feature.
    */
   public XSOMParser() {
       this(new JAXPParser());
   }
    
   /**
    * Creates a new XSOMParser that uses the given SAXParserFactory
    * for creating new SAX parsers.
    * 
    * The caller needs to configure
    * it properly. Don't forget to call <code>setNamespaceAware(true)</code>
    * or you'll see some strange errors.
    */
   public XSOMParser( SAXParserFactory factory ) {
       this( new JAXPParser(factory) );
   }
    
   /**
    * Creates a new XSOMParser that reads XML Schema from non-standard
    * inputs.
    * 
    * By implementing the {@link XMLParser} interface, XML Schema
    * can be read from something other than XML.
    * 
    * @param   parser
    *      This parser will be called to parse XML Schema documents.
    */
    public XSOMParser(XMLParser parser) {
        context = new ParserContext(this,parser);
    }

    /**
     * Parses a new XML Schema document.
     *
     * <p>
     * When using this method, XSOM does not know the system ID of
     * this document, therefore, when this stream contains relative
     * references to other schemas, XSOM will fail to resolve them.
     * To specify an system ID with a stream, use {@link InputSource}
     */
    public void parse( InputStream is ) throws SAXException {
        parse(new InputSource(is));
    }

    /**
     * Parses a new XML Schema document.
     *
     * <p>
     * When using this method, XSOM does not know the system ID of
     * this document, therefore, when this reader contains relative
     * references to other schemas, XSOM will fail to resolve them.
     * To specify an system ID with a reader, use {@link InputSource}
     */
    public void parse( Reader reader ) throws SAXException {
        parse(new InputSource(reader));
    }

    /**
     * Parses a new XML Schema document.
     */
    public void parse( File schema ) throws SAXException, IOException {
        parse(schema.toURL());
    }
    
    /**
     * Parses a new XML Schema document.
     */
    public void parse( URL url ) throws SAXException {
        parse( url.toExternalForm() );
    }
    
    /**
     * Parses a new XML Schema document.
     */
    public void parse( String systemId ) throws SAXException {
        parse(new InputSource(systemId));
    }
    
    /**
     * Parses a new XML Schema document.
     *
     * <p>
     * Note that if the {@link InputSource} does not have a system ID,
     * XSOM will fail to resolve them.
     */
    public void parse( InputSource source ) throws SAXException {
        context.parse(source);
    }
    
    
    
    /**
     * Gets the parser implemented as a ContentHandler.
     * 
     * One can feed XML Schema as SAX events to this interface to
     * parse a schema. To parse multiple schema files, feed multiple
     * sets of events.
     * 
     * <p>
     * If you don't send a complete event sequence from a startDocument
     * event to an endDocument event, the state of XSOMParser can become
     * unstable. This sometimes happen when you encounter an error while
     * generating SAX events. Don't call the getResult method in that case.
     * 
     * <p>
     * This way of reading XML Schema can be useful when XML Schema is
     * not available as a stand-alone XML document.
     * For example, one can feed XML Schema inside a WSDL document.
     */
    public ContentHandler getParserHandler() {
        NGCCRuntimeEx runtime = new NGCCRuntimeEx(context);
        Schema s = new Schema(runtime,false,null);
        runtime.setRootHandler(s);
        return runtime;
    }
    
    /**
     * Gets the parsed result. Don't call this method until
     * you parse all the schemas.
     * 
     * @return
     *      If there was any parse error, this method returns null.
     *      To receive error information, specify your error handler
     *      through the setErrorHandler method.
     * @exception SAXException
     *      This exception will never be thrown unless it is thrown
     *      by an error handler.
     */
    public XSSchemaSet getResult() throws SAXException {
        return context.getResult();
    }

    /**
     * Gets the set of {@link SchemaDocument} that represents
     * parsed documents so far.
     *
     * @return
     *      can be empty but never null.
     */
    public Set<SchemaDocument> getDocuments() {
        return context.getSchemaDocuments();
    }
    
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }
    /**
     * Set an entity resolver that is used to resolve things
     * like {@code <xsd:import>} and {@code <xsd:include>}.
     */
    public void setEntityResolver( EntityResolver resolver ) {
        this.entityResolver = resolver;
    }
    public ErrorHandler getErrorHandler() {
        return userErrorHandler;
    }
    /**
     * Set an error handler that receives all the errors encountered
     * during the parsing.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.userErrorHandler = errorHandler;
    }

    /**
     * Sets the annotation parser.
     * 
     * Annotation parser can be used to parse application-specific
     * annotations inside a schema.
     * 
     * <p>
     * For each annotation, new instance of this class will be
     * created and used to parse {@code <xs:annotation>}.
     */
    public void setAnnotationParser( final Class annParser ) {
        setAnnotationParser( new AnnotationParserFactory() {
            public AnnotationParser create() {
                try {
                    return (AnnotationParser)annParser.newInstance();
                } catch( InstantiationException e ) {
                    throw new InstantiationError(e.getMessage());
                } catch( IllegalAccessException e ) {
                    throw new IllegalAccessError(e.getMessage());
                }
            }
        });
    }

    /**
     * Sets the annotation parser factory.
     * 
     * <p>
     * The specified factory will be used to create AnnotationParsers.
     */
    public void setAnnotationParser( AnnotationParserFactory factory ) {
        this.apFactory = factory;
    }

    public AnnotationParserFactory getAnnotationParserFactory() {
        return apFactory;
    }
}
