/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

/* $Id: StAXEventConnector.java,v 1.8 2008-11-10 13:32:01 snajper Exp $
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
package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is a simple utility class that adapts StAX events from an
 * {@link XMLEventReader} to unmarshaller events on a
 * {@link XmlVisitor}, bridging between the two
 * parser technologies.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
final class StAXEventConnector extends StAXConnector {

    // StAX event source
    private final XMLEventReader staxEventReader;

    /** Current event. */
    private XMLEvent event;

    /**
     * Shared and reused {@link Attributes}.
     */
    private final AttributesImpl attrs = new AttributesImpl();

    /**
     * SAX may fire consective characters event, but we don't allow it.
     * so use this buffer to perform buffering.
     */
    private final StringBuilder buffer = new StringBuilder();

    private boolean seenText;

    /**
     * Construct a new StAX to SAX adapter that will convert a StAX event
     * stream into a SAX event stream.
     * 
     * @param staxCore
     *                StAX event source
     * @param visitor
     *                sink
     */
    public StAXEventConnector(XMLEventReader staxCore, XmlVisitor visitor) {
        super(visitor);
        staxEventReader = staxCore;
    }

    public void bridge() throws XMLStreamException {

        try {
            // remembers the nest level of elements to know when we are done.
            int depth=0;

            event = staxEventReader.peek();

            if( !event.isStartDocument() && !event.isStartElement() )
                throw new IllegalStateException();

            // if the parser is on START_DOCUMENT, skip ahead to the first element
            do {
                event = staxEventReader.nextEvent();
            } while( !event.isStartElement() );

            handleStartDocument(event.asStartElement().getNamespaceContext());

            OUTER:
            while(true) {
                // These are all of the events listed in the javadoc for
                // XMLEvent.
                // The spec only really describes 11 of them.
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT :
                        handleStartElement(event.asStartElement());
                        depth++;
                        break;
                    case XMLStreamConstants.END_ELEMENT :
                        depth--;
                        handleEndElement(event.asEndElement());
                        if(depth==0)    break OUTER;
                        break;
                    case XMLStreamConstants.CHARACTERS :
                    case XMLStreamConstants.CDATA :
                    case XMLStreamConstants.SPACE :
                        handleCharacters(event.asCharacters());
                        break;
                }


                event=staxEventReader.nextEvent();
            }

            handleEndDocument();
            event = null; // avoid keeping a stale reference
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    protected Location getCurrentLocation() {
        return event.getLocation();
    }

    protected String getCurrentQName() {
        QName qName;
        if(event.isEndElement())
            qName = event.asEndElement().getName();
        else
            qName = event.asStartElement().getName();
        return getQName(qName.getPrefix(), qName.getLocalPart());
    }


    private void handleCharacters(Characters event) throws SAXException, XMLStreamException {
        if(!predictor.expectText())
            return;     // text isn't expected. simply skip

        seenText = true;

        // check the next event
        XMLEvent next;
        while(true) {
            next = staxEventReader.peek();
            if(!isIgnorable(next))
                break;
            staxEventReader.nextEvent();
        }

        if(isTag(next)) {
            // this is by far the common case --- you have <foo>abc</foo> or <foo>abc<bar/>...</foo>
            visitor.text(event.getData());
            return;
        }

        // otherwise we have things like "abc<!-- test -->def".
        // concatenate all text
        buffer.append(event.getData());

        while(true) {
            while(true) {
                next = staxEventReader.peek();
                if(!isIgnorable(next))
                    break;
                staxEventReader.nextEvent();
            }

            if(isTag(next)) {
                // found all adjacent text
                visitor.text(buffer);
                buffer.setLength(0);
                return;
            }

            buffer.append(next.asCharacters().getData());
            staxEventReader.nextEvent();    // consume
        }
    }

    private boolean isTag(XMLEvent event) {
        int eventType = event.getEventType();
        return eventType==XMLEvent.START_ELEMENT || eventType==XMLEvent.END_ELEMENT;
    }

    private boolean isIgnorable(XMLEvent event) {
        int eventType = event.getEventType();
        return eventType==XMLEvent.COMMENT || eventType==XMLEvent.PROCESSING_INSTRUCTION;
    }

    private void handleEndElement(EndElement event) throws SAXException {
        if(!seenText && predictor.expectText()) {
            visitor.text("");
        }

        // fire endElement
        QName qName = event.getName();
        tagName.uri = fixNull(qName.getNamespaceURI());
        tagName.local = qName.getLocalPart();
        visitor.endElement(tagName);

        // end namespace bindings
        for( Iterator<Namespace> i = event.getNamespaces(); i.hasNext();) {
            String prefix = fixNull(i.next().getPrefix());  // be defensive
            visitor.endPrefixMapping(prefix);
        }

        seenText = false;
    }

    private void handleStartElement(StartElement event) throws SAXException {
        // start namespace bindings
        for (Iterator i = event.getNamespaces(); i.hasNext();) {
            Namespace ns = (Namespace)i.next();
            visitor.startPrefixMapping(
                fixNull(ns.getPrefix()),
                fixNull(ns.getNamespaceURI()));
        }

        // fire startElement
        QName qName = event.getName();
        tagName.uri = fixNull(qName.getNamespaceURI());
        String localName = qName.getLocalPart();
        tagName.uri = fixNull(qName.getNamespaceURI());
        tagName.local = localName;
        tagName.atts = getAttributes(event);
        visitor.startElement(tagName);

        seenText = false;
    }



    /**
     * Get the attributes associated with the given START_ELEMENT StAXevent.
     *
     * @return the StAX attributes converted to an org.xml.sax.Attributes
     */
    private Attributes getAttributes(StartElement event) {
        attrs.clear();

        // in SAX, namespace declarations are not part of attributes by default.
        // (there's a property to control that, but as far as we are concerned
        // we don't use it.) So don't add xmlns:* to attributes.

        // gather non-namespace attrs
        for (Iterator i = event.getAttributes(); i.hasNext();) {
            Attribute staxAttr = (Attribute)i.next();

            QName name = staxAttr.getName();
            String uri = fixNull(name.getNamespaceURI());
            String localName = name.getLocalPart();
            String prefix = name.getPrefix();
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
}
