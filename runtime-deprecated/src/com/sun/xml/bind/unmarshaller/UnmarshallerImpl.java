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

package com.sun.xml.bind.unmarshaller;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.helpers.AbstractUnmarshallerImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.DefaultJAXBContextImpl;
import com.sun.xml.bind.ErrorHandlerToEventHandler;
import com.sun.xml.bind.TypeRegistry;
import com.sun.xml.bind.validator.DOMLocator;
import com.sun.xml.bind.validator.Locator;
import com.sun.xml.bind.validator.SAXLocator;
import com.sun.xml.bind.validator.ValidatingUnmarshaller;

/**
 * Default Unmarshall implementation.
 * 
 * <p>
 * This class can be extended by the generated code to provide
 * type-safe unmarshall methods.
 *
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com>Kohsuke KAWAGUCHI</a>
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class UnmarshallerImpl extends AbstractUnmarshallerImpl
{
    /** parent JAXBContext object that created this unmarshaller */
    private DefaultJAXBContextImpl context = null;
    
    /** Type registry that stores schema information. */
    private final TypeRegistry registry;
    
    public UnmarshallerImpl( DefaultJAXBContextImpl context, TypeRegistry reg ) {
        
        this.context = context;
        this.registry = reg;

        // initialize datatype converter with ours
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
    }
    
    public UnmarshallerHandler getUnmarshallerHandler() {
        // TODO: use only one instance.
        
        // we don't know the Locator to be used,
        // but SAXLocator would always be a good default,
        // as the source of SAX2 events can always set org.xml.sax.Locator.
        return createUnmarshallerHandler(new SAXLocator());
    }
    
    
    
    /**
     * Creates and configures a new unmarshalling pipe line.
     * Depending on the setting, we put a validator as a filter.
     * 
     * @return
     *      A component that implements both UnmarshallerHandler
     *      and ValidationEventHandler. All the parsing errors
     *      should be reported to this error handler for the unmarshalling
     *      process to work correctly.
     * 
     * @param locator
     *      The object that is responsible to obtain the source
     *      location information for {@link ValidationEvent}s.
     */
    public SAXUnmarshallerHandler createUnmarshallerHandler( Locator locator ) {

        SAXUnmarshallerHandler unmarshaller =
            new SAXUnmarshallerHandlerImpl( this, registry );
        
        try {
            
            // use the simple check to determine if validation is on
            if( isValidating() ) { 
                // if the validation is turned on, insert another
                // component into the event pipe line.
                unmarshaller = ValidatingUnmarshaller.create(
                    context.getGrammar(), unmarshaller, locator );
            }
        } catch( Exception e ) {
            e.printStackTrace(); //TODO: fix error handling
        }
        
        return unmarshaller;
    }


    protected Object unmarshal( XMLReader reader, InputSource source ) throws JAXBException {
        
        SAXLocator locator = new SAXLocator();
        SAXUnmarshallerHandler handler = createUnmarshallerHandler(locator);
        
        reader.setContentHandler(handler);
        // saxErrorHandler will be set by the createUnmarshallerHandler method.
        // configure XMLReader so that the error will be sent to it.
        // This is essential for the UnmarshallerHandler to be able to abort
        // unmarshalling when an error is found.
        //
        // Note that when this XMLReader is provided by the client code,
        // it might be already configured to call a client error handler.
        // This will clobber such handler, if any.
        //
        // Ryan noted that we might want to report errors to such a client
        // error handler as well.
        reader.setErrorHandler(
            new ErrorHandlerToEventHandler(handler,locator));
        
        try {
            reader.parse(source);
        } catch( IOException e ) {
            throw new JAXBException(e);
        } catch( SAXException e ) {
            throw createUnmarshalException(e);
        }
        
        return handler.getResult();
    }
    
    public final Object unmarshal( Node node ) throws JAXBException {
        try {
            DOMScanner scanner = new DOMScanner();
            UnmarshallerHandler handler = createUnmarshallerHandler(new DOMLocator(scanner));
            
            if(node instanceof Element)
                scanner.parse((Element)node,handler);
            else
            if(node instanceof Document)
                scanner.parse(((Document)node).getDocumentElement(),handler);
            else
                // no other type of input is supported
                throw new IllegalArgumentException();
            
            return handler.getResult();
        } catch( SAXException e ) {
            throw createUnmarshalException(e);
        }
    }

    
    private static void _assert( boolean b, String msg ) {
        if( !b ) {
            throw new InternalError( msg );
        }
    }
    
}
