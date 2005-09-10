/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.bind.serializer;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Receives SAX2 events and send the equivalent events to
 * {@link com.sun.xml.bind.serializer.XMLSerializer}
 * 
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ContentHandlerAdaptor implements ContentHandler {

    /** Stores newly declared prefix-URI mapping. */
    private final ArrayList prefixMap = new ArrayList();
    
    /** Events will be sent to this object. */
    private final XMLSerializer serializer;
    
    private final StringBuffer text = new StringBuffer();
    
    
    public ContentHandlerAdaptor( XMLSerializer _serializer ) {
        this.serializer = _serializer;
    }
    
    

    public void startDocument() throws SAXException {
        prefixMap.clear();
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMap.add(prefix);
        prefixMap.add(uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        
        flushText();
        
        serializer.startElement(namespaceURI,localName);
        // fire attribute events
        for( int i=0; i<atts.getLength(); i++ ) {
            serializer.startAttribute( atts.getURI(i), atts.getLocalName(i) );
            serializer.text(atts.getValue(i));
            serializer.endAttribute();
        }
        // TODO: fire new prefix-URI association
        prefixMap.clear();
        serializer.endAttributes();
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        flushText();
        serializer.endElement();
    }
    
    private void flushText() throws SAXException {
        if( text.length()!=0 ) {
            serializer.text(text.toString());
            text.setLength(0);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        text.append(ch,start,length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        text.append(ch,start,length);
    }



    public void setDocumentLocator(Locator locator) {
    }
    
    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

}
