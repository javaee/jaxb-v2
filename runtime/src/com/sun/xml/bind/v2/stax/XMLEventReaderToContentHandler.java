/* $Id: XMLEventReaderToContentHandler.java,v 1.1 2005-04-21 00:01:56 kohsuke Exp $
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.xml.bind.v2.stax;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is a simple utility class that adapts StAX events from an
 * {@link javax.xml.stream.XMLEventReader} to SAX events on a
 * {@link org.xml.sax.ContentHandler}, bridging between the two
 * parser technologies.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public class XMLEventReaderToContentHandler implements StAXReaderToContentHandler {

    // StAX event source
    private final XMLEventReader staxEventReader;

    // SAX event sink
    private final ContentHandler saxHandler;
    
    /**
     * Construct a new StAX to SAX adapter that will convert a StAX event
     * stream into a SAX event stream.
     * 
     * @param staxCore
     *                StAX event source
     * @param saxCore
     *                SAXevent sink
     */
    public XMLEventReaderToContentHandler(XMLEventReader staxCore, ContentHandler saxCore) {
        staxEventReader = staxCore;
        saxHandler = saxCore;
    }

    /*
     * @see StAXReaderToContentHandler#bridge()
     */
    public void bridge() throws XMLStreamException {

        try {
            // remembers the nest level of elements to know when we are done.
            int depth=0;

            XMLEvent event = staxEventReader.peek();

            if( !event.isStartDocument() && !event.isStartElement() )
                throw new IllegalStateException();

            // if the parser is on START_DOCUMENT, skip ahead to the first element
            while( !event.isStartElement() ) {
                event = staxEventReader.nextEvent();
            }

            handleStartDocument(event);

            do {
                // These are all of the events listed in the javadoc for
                // XMLEvent.
                // The spec only really describes 11 of them.
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT :
                        depth++;
                        handleStartElement(event.asStartElement());
                        break;
                    case XMLStreamConstants.END_ELEMENT :
                        handleEndElement(event.asEndElement());
                        depth--;
                        break;
                    case XMLStreamConstants.CHARACTERS :
                        handleCharacters(event.asCharacters());
                        break;
                    case XMLStreamConstants.ENTITY_REFERENCE :
                        handleEntityReference();
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION :
                        handlePI((ProcessingInstruction)event);
                        break;
                    case XMLStreamConstants.COMMENT :
                        handleComment();
                        break;
                    case XMLStreamConstants.DTD :
                        handleDTD();
                        break;
                    case XMLStreamConstants.ATTRIBUTE :
                        handleAttribute();
                        break;
                    case XMLStreamConstants.NAMESPACE :
                        handleNamespace();
                        break;
                    case XMLStreamConstants.CDATA :
                        handleCDATA();
                        break;
                    case XMLStreamConstants.ENTITY_DECLARATION :
                        handleEntityDecl();
                        break;
                    case XMLStreamConstants.NOTATION_DECLARATION :
                        handleNotationDecl();
                        break;
                    case XMLStreamConstants.SPACE :
                        handleSpace();
                        break;
                    default :
                        throw new InternalError("processing event: " + event);
                }

                event=staxEventReader.nextEvent();
            } while (depth!=0);

            handleEndDocument();
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleEndDocument() throws SAXException {
        saxHandler.endDocument();
    }

    private void handleStartDocument(final XMLEvent event) throws SAXException {
        saxHandler.setDocumentLocator(new Locator() {
            public int getColumnNumber() {
                return event.getLocation().getColumnNumber();
            }
            public int getLineNumber() {
                return event.getLocation().getLineNumber();
            }
            public String getPublicId() {
                return event.getLocation().getPublicId();
            }
            public String getSystemId() {
                return event.getLocation().getSystemId();
            }
        });
        saxHandler.startDocument();
    }

    private void handlePI(ProcessingInstruction event)
        throws XMLStreamException {
        try {
            saxHandler.processingInstruction(
                event.getTarget(),
                event.getData());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleCharacters(Characters event) throws XMLStreamException {
        try {
            saxHandler.characters(
                event.getData().toCharArray(),
                0,
                event.getData().length());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleEndElement(EndElement event) throws XMLStreamException {
        QName qName = event.getName();

        try {
            // fire endElement
            saxHandler.endElement(
                qName.getNamespaceURI(),
                qName.getLocalPart(),
                qName.toString());

            // end namespace bindings
            for( Iterator i = event.getNamespaces(); i.hasNext();) {
                String prefix = (String)i.next();
                if( prefix == null ) { // true for default namespace
                    prefix = "";
                }
                saxHandler.endPrefixMapping(prefix);
            }
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleStartElement(StartElement event)
        throws XMLStreamException {
        try {
            // start namespace bindings
            for (Iterator i = event.getNamespaces(); i.hasNext();) {
                String prefix = ((Namespace)i.next()).getPrefix();
                if (prefix == null) { // true for default namespace
                    prefix = "";
                }
                saxHandler.startPrefixMapping(
                    prefix,
                    event.getNamespaceURI(prefix));
            }

            // fire startElement
            QName qName = event.getName();
            String prefix = qName.getPrefix();
            String rawname;
            if (prefix == null || prefix.length() == 0)
                rawname = qName.getLocalPart();
            else
                rawname = prefix + ':' + qName.getLocalPart();
            Attributes saxAttrs = getAttributes(event);
            saxHandler.startElement(
                qName.getNamespaceURI(),
                qName.getLocalPart(),
                rawname,
                saxAttrs);
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Get the attributes associated with the given START_ELEMENT StAXevent.
     *
     * @return the StAX attributes converted to an org.xml.sax.Attributes
     */
    private Attributes getAttributes(StartElement event) {
        AttributesImpl attrs = new AttributesImpl();

        if ( !event.isStartElement() ) {
            throw new InternalError(
                "getAttributes() attempting to process: " + event);
        }
        
        // in SAX, namespace declarations are not part of attributes by default.
        // (there's a property to control that, but as far as we are concerned
        // we don't use it.) So don't add xmlns:* to attributes.

        // gather non-namespace attrs
        for (Iterator i = event.getAttributes(); i.hasNext();) {
            Attribute staxAttr = (javax.xml.stream.events.Attribute)i.next();
            
            String uri = staxAttr.getName().getNamespaceURI();
            if (uri == null)
                uri = "";
            String localName = staxAttr.getName().getLocalPart();
            String prefix = staxAttr.getName().getPrefix();
            String qName;
            if (prefix == null || prefix.length() == 0)
                qName = localName;
            else
                qName = prefix + ':' + localName;
            String type = staxAttr.getDTDType();
            String value = staxAttr.getValue();
            
            attrs.addAttribute(uri, localName, qName, type, value);
        }

        return attrs;
    }

    private void handleNamespace() {
        // no-op ???
        // namespace events don't normally occur outside of a startElement
        // or endElement
    }

    private void handleAttribute() {
        // no-op ???
        // attribute events don't normally occur outside of a startElement
        // or endElement
    }

    private void handleDTD() {
        // no-op ???
        // it seems like we need to pass this info along, but how?
    }

    private void handleComment() {
        // no-op ???
    }

    private void handleEntityReference() {
        // no-op ???
    }

    private void handleSpace() {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleNotationDecl() {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleEntityDecl() {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleCDATA() {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }
}
