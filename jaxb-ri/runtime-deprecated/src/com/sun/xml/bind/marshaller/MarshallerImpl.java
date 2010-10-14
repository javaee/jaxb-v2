/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.bind.marshaller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.helpers.AbstractMarshallerImpl;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.serializer.XMLSerializable;

/**
 * Implementation of {@link Marshaller} interface for JAXB RI.
 * 
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class MarshallerImpl extends AbstractMarshallerImpl
{
    /** Indentation string. Default is four whitespaces. */
    private String indent = "    ";
    
    /** Object that handles character escaping. */
    private CharacterEscapeHandler escapeHandler = null; 
    
    public MarshallerImpl() {
        // initialize datatype converter with ours
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
    }
    
    public void marshal(Object obj, Result result) throws JAXBException {
        if (!(obj instanceof XMLSerializable) || obj == null)
            throw new MarshalException( 
                Messages.format( Messages.NOT_MARSHALLABLE ) );

        XMLSerializable so = (XMLSerializable) obj;

        if (result instanceof SAXResult) {
            write(so, ((SAXResult) result).getHandler());
            return;
        }
        if (result instanceof DOMResult) {
            Node node = ((DOMResult) result).getNode();

            if (node == null) {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.newDocument();
                    ((DOMResult) result).setNode(doc);
                    write(so, new SAX2DOMEx(doc));
                } catch (ParserConfigurationException pce) {
                    throw new InternalError();
                }
            } else {
                write(so, new SAX2DOMEx(node));
            }

            return;
        }
        if (result instanceof StreamResult) {
            StreamResult sr = (StreamResult) result;
            XMLWriter w = null;

            if (sr.getWriter() != null)
                w = createWriter(sr.getWriter());
            else if (sr.getOutputStream() != null)
                w = createWriter(sr.getOutputStream());
            else if (sr.getSystemId() != null) {
                String fileURL = sr.getSystemId();

                if (fileURL.startsWith("file:///")) {
                    if (fileURL.substring(8).indexOf(":") > 0)
                        fileURL = fileURL.substring(8);
                    else
                        fileURL = fileURL.substring(7);
                } // otherwise assume that it's a file name

                try {
                    w = createWriter(new FileOutputStream(fileURL));
                } catch (IOException e) {
                    throw new MarshalException(e);
                }
            }

            if (w == null)
                throw new IllegalArgumentException();

            write(so, w);
            return;
        }

        // unsupported parameter type
        throw new MarshalException( 
            Messages.format( Messages.UNSUPPORTED_RESULT ) );
    }
    
    private void write( XMLSerializable obj, ContentHandler writer )
        throws JAXBException {

        try {
            if( getSchemaLocation()!=null || getNoNSSchemaLocation()!=null ) {
                // if we need to add xsi:schemaLocation or its brother,
                // throw in the component to do that.
                writer = new SchemaLocationFilter(
                    getSchemaLocation(),
                    getNoNSSchemaLocation(),
                    writer );
            }
            
            SAXMarshaller serializer = 
                new SAXMarshaller(writer,this);
        
            // set a DocumentLocator that doesn't provide any information
            writer.setDocumentLocator( new LocatorImpl() );
            writer.startDocument();
            obj.serializeElements(serializer);
            writer.endDocument();
        } catch( SAXException e ) {
            throw new MarshalException(e);
        }
    }
    
    
    //
    //
    // create XMLWriter by specifing various type of output.
    //
    //
    
    protected CharacterEscapeHandler createEscapeHandler( String encoding ) {
        if( escapeHandler!=null )
            // user-specified one takes precedence.
            return escapeHandler;
        
        // otherwise try to find one from the encoding
        try {
            // try new JDK1.4 NIO
            return new NioEscapeHandler( getJavaEncoding(encoding) );
        } catch( Throwable e ) {
            // if that fails, fall back to the dumb mode
            return DumbEscapeHandler.theInstance;
        }
    }
            
    public XMLWriter createWriter( Writer w, String encoding ) throws JAXBException {
        
        CharacterEscapeHandler ceh = createEscapeHandler(encoding);
        
        if(isFormattedOutput()) {
            DataWriter d = new DataWriter(w,encoding,ceh);
            d.setIndentStep(indent);
            return d;
        } 
        else
            return new XMLWriter(w,encoding,ceh);
    }

    public XMLWriter createWriter(Writer w) throws JAXBException{
        return createWriter(w, getEncoding());
    }
    
    public XMLWriter createWriter( OutputStream os ) throws JAXBException {
        return createWriter(os, getEncoding());
    }
    
    public XMLWriter createWriter( OutputStream os, String encoding ) throws JAXBException {
        try {
            return createWriter(
                new OutputStreamWriter(os,getJavaEncoding(encoding)),
                encoding );
        } catch( UnsupportedEncodingException e ) {
            throw new MarshalException(
                Messages.format( Messages.UNSUPPORTED_ENCODING, encoding ),
                e );
        }
    }
    
    
    public Object getProperty(String name) throws PropertyException {
        if( INDENT_STRING.equals(name) )
            return indent;
        if( ENCODING_HANDLER.equals(name) )
            return escapeHandler;

        return super.getProperty(name);
    }

    public void setProperty(String name, Object value) throws PropertyException {
        if( INDENT_STRING.equals(name) && value instanceof String ) {
            indent = (String)value;
            return;
        }
        if( ENCODING_HANDLER.equals(name) ) {
            escapeHandler = (CharacterEscapeHandler)value;
            return;
        }
            
        super.setProperty(name, value);
    }
    
    private static final String INDENT_STRING = "com.sun.xml.bind.indentString"; 
    private static final String ENCODING_HANDLER = "com.sun.xml.bind.characterEscapeHandler"; 
}
