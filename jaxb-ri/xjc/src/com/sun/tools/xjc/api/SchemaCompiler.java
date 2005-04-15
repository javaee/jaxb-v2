/*
 * @(#)$Id: SchemaCompiler.java,v 1.1 2005-04-15 20:08:58 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.api;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Schema-to-Java compiler.
 * 
 * <p>
 * The caller can parse multiple schema documents,
 * JAXB external binding files (or potentially WSDL
 * and JSR-109.next mapping files in the future).
 * 
 * <p>
 * All the errors found during this process will be sent
 * to the registered {@link ErrorListener}.
 * 
 * <p>
 * Once all the documents are parsed, call the {@link #bind()}
 * method to get the compiled {@link JAXBModel} object.
 * 
 * 
 * <h2>Tips: namespace URI -> package customization</h2>
 * <p>
 * The caller can feed the following synthesized schema
 * to achive the namespace URI -> Java package customization:
 * <pre><xmp>
 * <schema targetNamespace="xml.namespace.uri"
 *   xmlns="http://www.w3.org/2001/XMLSchema"
 *   xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
 *   jaxb:version="1.0">
 *   <annotation><appinfo>
 *     <jaxb:schemaBindings>
 *       <jaxb:package name="java.package.name"/>
 *     </jaxb:schemaBindings>
 *   </appinfo></annotation>
 * </schema>
 * </xmp></pre>
 * Feed this synthesized schema document for each namespace URI
 * you need to map.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface SchemaCompiler {
    /**
     * Parses schemas or external bindings
     * through SAX events by feeding events into
     * SAX {@link ContentHandler}.
     * 
     * @param systemId
     *      The system ID of the document to be read in.
     * 
     * @see #parseSchema(String, XMLStreamReader)
     */
    ContentHandler getParserHandler( String systemId );
    
    /**
     * Parses a schema or an external binding file
     * from an external source.
     * 
     * @param source
     *      Its system Id must be set to an absolute URI.
     */
    void parseSchema( InputSource source );
    
    /**
     * Parses a schema or an external binding file
     * from the specified DOM element.
     * 
     * <p>
     * The given DOM element is treated as if it's the root of a
     * virtual document.
     * 
     * <p>
     * XJC will not be able to print location information for
     * errors found in this document, since DOM doesn't have them.
     * For this reason, use of this method is strongly discouraged.
     * 
     * @param systemId
     *      We need an absolute system ID that uniquely designates the virtual
     *      document. This should be different from the system ID of
     *      the document which contains this element.
     *      <p>
     *      One way to do that is by adding a fragment identifier
     *      to the system ID of the document. For example, if the document
     *      is "foo.wsdl" and you are passing in its types section, you
     *      can use an unique identifier like "foo.wsdl#types"
     */
    void parseSchema( String systemId, Element element );
    
    /**
     * Parses a schema or an external binding file
     * from the given source.
     * 
     * <p>
     * A stream reader must be pointing at the element or
     * at the start of the document.
     * XML is parsed until the corresponding end tag, then the
     * sub tree is processed as a schema document.
     * 
     * <p>
     * When this method returns successfully, the parser is at
     * the next token of the end element.
     * 
     * @param systemId
     *      The absolute system ID of the document that is being parsed.
     *      This information is necessary to avoid double-inclusion
     *      and etc.
     * 
     *      Note that {@link XMLStreamReader#getLocation()} only
     *      returns the system ID of the entity it is parsing, not
     *      necessarily the system ID of the document itself.
     * 
     * @throws XMLStreamException
     *      If an error happens while parsing a document.
     *      Note that not only the parser but also the XJC itself
     *      may throw this error (as a result of the additional validation
     *      for example.)
     */
    void parseSchema( String systemId, XMLStreamReader reader ) throws XMLStreamException;
    
    void setErrorListener( ErrorListener errorListener );
    void setEntityResolver( EntityResolver entityResolver );


    /**
     * Sets the default Java package name into which the generated code will be placed.
     *
     * <p>
     * Customizations in the binding files/schemas will have precedence over this setting.
     * Set to null to use the default package name computation algorithm as specified by
     * the JAXB spec (which is the default behavior.)
     *
     * <p>
     * Initially this parameter is set to null.
     *
     * @param packageName
     *      Java pckage name such as "org.foo.bar". Use "" to represent the root package,
     *      and null to defer to the default computation algorithm.
     */
    void setDefaultPackageName( String packageName );

    /**
     * Sets the {@link ClassNameAllocator} to be used for the binding operation.
     *
     * <p>
     * This mechanism would allow the caller to participate in the binding operation.
     *
     * @see ClassNameAllocator
     */
    void setClassNameAllocator( ClassNameAllocator allocator );

    /**
     * Obtains the compiled schema object model.
     * 
     * Once this method is called, no other method should be
     * invoked on the {@link SchemaCompiler}.
     * 
     * @return
     *      null if the compilation fails. The errors should have been
     *      delivered to the registered error handler in such a case.
     */
    S2JJAXBModel bind();
}
