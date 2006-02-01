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
package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.util.FinalArrayList;
import com.sun.xml.bind.util.SAXException2;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Receives SAX2 events and send the equivalent events to
 * {@link XMLSerializer}
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ContentHandlerAdaptor extends DefaultHandler {

    /** Stores newly declared prefix-URI mapping. */
    private final FinalArrayList prefixMap = new FinalArrayList();
    
    /** Events will be sent to this object. */
    private final XMLSerializer serializer;
    
    private final StringBuffer text = new StringBuffer();
    
    
    ContentHandlerAdaptor( XMLSerializer _serializer ) {
        this.serializer = _serializer;
    }
    
    public void startDocument() {
        prefixMap.clear();
    }

    public void startPrefixMapping(String prefix, String uri) {
        prefixMap.add(prefix);
        prefixMap.add(uri);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        try {
            flushText();

            int len = atts.getLength();

            serializer.startElement(namespaceURI,localName,getPrefix(qName),null);
            // declare namespace events
            for( int i=0; i<len; i++ ) {
                String qname = atts.getQName(i);
                if(qname.startsWith("xmlns"))
                    continue;
                String prefix = getPrefix(qname);

                serializer.getNamespaceContext().declareNamespace(
                    atts.getURI(i), prefix, true );
            }
            for( int i=0; i<prefixMap.size(); i+=2 ) {
                String prefix = (String)prefixMap.get(i);
                serializer.getNamespaceContext().declareNamespace(
                    (String)prefixMap.get(i+1),
                    prefix,
                    prefix.length()!=0 );
            }

            serializer.endNamespaceDecls(null);
            // fire attribute events
            for( int i=0; i<len; i++ ) {
                // be defensive.
                if(atts.getQName(i).startsWith("xmlns"))
                    continue;
                serializer.attribute( atts.getURI(i), atts.getLocalName(i), atts.getValue(i));
            }
            prefixMap.clear();
            serializer.endAttributes();
        } catch (IOException e) {
            throw new SAXException2(e);
        } catch (XMLStreamException e) {
            throw new SAXException2(e);
        }
    }

    private String getPrefix(String qname) {
        int idx = qname.indexOf(':');
        String prefix = (idx==-1)?qname:qname.substring(0,idx);
        return prefix;
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        try {
            flushText();
            serializer.endElement();
        } catch (IOException e) {
            throw new SAXException2(e);
        } catch (XMLStreamException e) {
            throw new SAXException2(e);
        }
    }
    
    private void flushText() throws SAXException, IOException, XMLStreamException {
        if( text.length()!=0 ) {
            serializer.text(text.toString(),null);
            text.setLength(0);
        }
    }

    public void characters(char[] ch, int start, int length) {
        text.append(ch,start,length);
    }
}
