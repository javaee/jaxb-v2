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

import java.util.Enumeration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

import primer.PurchaseOrderType;

/*
 * @(#)$Id: Splitter.java,v 1.1 2007-12-05 00:49:34 kohsuke Exp $
 */
 
/**
 * This object implements XMLFilter and monitors the incoming SAX
 * events. Once it hits a purchaseOrder element, it creates a new
 * unmarshaller and unmarshals one purchase order.
 * 
 * <p>
 * Once finished unmarshalling it, we will process it, then move
 * on to the next purchase order.
 */
public class Splitter extends XMLFilterImpl {
    
    public Splitter( JAXBContext context ) {
        this.context = context;
    }
    
    /**
     * We will create unmarshallers from this context.
     */
    private final JAXBContext context;
    
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
            
        if( depth!= 0 ) {
            // we are in the middle of forwarding events.
            // continue to do so.
            depth++;
            super.startElement(namespaceURI, localName, qName, atts);
            return;
        }
        
        if( namespaceURI.equals("") && localName.equals("purchaseOrder") ) {
            // start a new unmarshaller
            Unmarshaller unmarshaller;
            try {
                unmarshaller = context.createUnmarshaller();
            } catch( JAXBException e ) {
                // there's no way to recover from this error.
                // we will abort the processing.
                throw new SAXException(e);
            }
            unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
            
            // set it as the content handler so that it will receive
            // SAX events from now on.
            setContentHandler(unmarshallerHandler);
            
            // fire SAX events to emulate the start of a new document.
            unmarshallerHandler.startDocument();
            unmarshallerHandler.setDocumentLocator(locator);
            
            Enumeration e = namespaces.getPrefixes();
            while( e.hasMoreElements() ) {
                String prefix = (String)e.nextElement();
                String uri = namespaces.getURI(prefix);
                
                unmarshallerHandler.startPrefixMapping(prefix,uri);
            }
            String defaultURI = namespaces.getURI("");
            if( defaultURI!=null )
                unmarshallerHandler.startPrefixMapping("",defaultURI);

            super.startElement(namespaceURI, localName, qName, atts);
            
            // count the depth of elements and we will know when to stop.
            depth=1;
        }
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        
        // forward this event
        super.endElement(namespaceURI, localName, qName);
        
        if( depth!=0 ) {
            depth--;
            if( depth==0 ) {
                // just finished sending one chunk.
                
                // emulate the end of a document.
                Enumeration e = namespaces.getPrefixes();
                while( e.hasMoreElements() ) {
                    String prefix = (String)e.nextElement();
                    unmarshallerHandler.endPrefixMapping(prefix);
                }
                String defaultURI = namespaces.getURI("");
                if( defaultURI!=null )
                    unmarshallerHandler.endPrefixMapping("");
                unmarshallerHandler.endDocument();
                
                // stop forwarding events by setting a dummy handler.
                // XMLFilter doesn't accept null, so we have to give it something,
                // hence a DefaultHandler, which does nothing.
                setContentHandler(new DefaultHandler());
                
                // then retrieve the fully unmarshalled object
                try {
                    JAXBElement<PurchaseOrderType> result = 
			(JAXBElement<PurchaseOrderType>)unmarshallerHandler.getResult();
                    
                    // process this new purchase order
                    process(result.getValue());
                } catch( JAXBException je ) {
                    // error was found during the unmarshalling.
                    // you can either abort the processing by throwing a SAXException,
                    // or you can continue processing by returning from this method.
                    System.err.println("unable to process an order at line "+
                        locator.getLineNumber() );
                    return;
                }
                
                unmarshallerHandler = null;
            }
        }
    }
    
    public void process( PurchaseOrderType order ) {
        System.out.println("this order will be shipped to "
            + order.getShipTo().getName() );
    }
    
    /**
     * Remembers the depth of the elements as we forward
     * SAX events to a JAXB unmarshaller.
     */
    private int depth;
    
    /**
     * Reference to the unmarshaller which is unmarshalling
     * an object.
     */
    private UnmarshallerHandler unmarshallerHandler;


    /**
     * Keeps a reference to the locator object so that we can later
     * pass it to a JAXB unmarshaller.
     */
    private Locator locator;
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }


    /**
     * Used to keep track of in-scope namespace bindings.
     * 
     * For JAXB unmarshaller to correctly unmarshal documents, it needs
     * to know all the effective namespace declarations.
     */
    private NamespaceSupport namespaces = new NamespaceSupport();
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaces.pushContext();
        namespaces.declarePrefix(prefix,uri);
        
        super.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        namespaces.popContext();
        
        super.endPrefixMapping(prefix);
    }
}
