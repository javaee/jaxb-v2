/* $Id: StAXStreamConnector.java,v 1.9.2.2 2006-08-07 14:44:10 sandoz Exp $
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

import java.lang.reflect.Constructor;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.bind.WhiteSpaceProcessor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads XML from StAX {@link XMLStreamReader} and
 * feeds events to {@link XmlVisitor}.
 * <p>
 * TODO:
 * Finding the optimized FI implementations is a bit hacky and not very
 * extensible. Can we use the service provider mechnism in general for 
 * concrete implementations of StAXConnector.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @author Kohsuke Kawaguchi
 * @version JAXB 2.0
 */
class StAXStreamConnector extends StAXConnector {

    /**
     * Creates a {@link StAXConnector} from {@link XMLStreamReader}.
     *
     * This method checks if the parser is FI parser and acts accordingly.
     */
    public static StAXConnector create(XMLStreamReader reader, XmlVisitor visitor) {
        // try optimized codepath
        final Class readerClass = reader.getClass();
        System.out.println(readerClass.getName());
        if (readerClass==ENHANCED_FI_STAX_READER_CLASS && ENHANCED_FI_CONNECTOR_CTOR!=null) {
            try {
                System.out.println("ENHANCED_FI_CONNECTOR_CTOR");
                return ENHANCED_FI_CONNECTOR_CTOR.newInstance(reader,visitor);
            } catch (Exception t) {
            }
        }
        if (readerClass==FI_STAX_READER_CLASS && FI_CONNECTOR_CTOR!=null) {
            try {
                System.out.println("FI_CONNECTOR_CTOR");
                return FI_CONNECTOR_CTOR.newInstance(reader,visitor);
            } catch (Exception t) {
            }
        }
        if (STAX_EX_READER_CLASS!=null && STAX_EX_READER_CLASS.isAssignableFrom(readerClass)) {
            try {
                return STAX_EX_CONNECTOR_CTOR.newInstance(reader,visitor);
            } catch (Exception t) {
            }
        }

        // Quick hack until SJSXP fixes 6270116
        boolean isZephyr = readerClass.getName().equals("com.sun.xml.stream.XMLReaderImpl");
        if(!isZephyr)
            visitor = new InterningXmlVisitor(visitor);
        return new StAXStreamConnector(reader,visitor);
    }



    // StAX event source
    private final XMLStreamReader staxStreamReader;

    /**
     * SAX may fire consective characters event, but we don't allow it.
     * so use this buffer to perform buffering.
     */
    protected final StringBuilder buffer = new StringBuilder();

    /**
     * Set to true if the text() event is reported, and therefore
     * the following text() event should be suppressed.
     */
    protected boolean textReported = false;

    protected StAXStreamConnector(XMLStreamReader staxStreamReader, XmlVisitor visitor) {
        super(visitor);
        this.staxStreamReader = staxStreamReader;
    }

    public void bridge() throws XMLStreamException {

        try {
            // remembers the nest level of elements to know when we are done.
            int depth=0;

            // if the parser is at the start tag, proceed to the first element
            int event = staxStreamReader.getEventType();
            if(event == XMLStreamConstants.START_DOCUMENT) {
                // nextTag doesn't correctly handle DTDs
                while( !staxStreamReader.isStartElement() )
                    event = staxStreamReader.next();
            }


            if( event!=XMLStreamConstants.START_ELEMENT)
                throw new IllegalStateException("The current event is not START_ELEMENT\n but " + event);

            handleStartDocument(staxStreamReader.getNamespaceContext());

            OUTER:
            while(true) {
                // These are all of the events listed in the javadoc for
                // XMLEvent.
                // The spec only really describes 11 of them.
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT :
                        handleStartElement();
                        depth++;
                        break;
                    case XMLStreamConstants.END_ELEMENT :
                        depth--;
                        handleEndElement();
                        if(depth==0)    break OUTER;
                        break;
                    case XMLStreamConstants.CHARACTERS :
                    case XMLStreamConstants.CDATA :
                    case XMLStreamConstants.SPACE :
                        handleCharacters();
                        break;
                    // otherwise simply ignore
                }

                event=staxStreamReader.next();
            }

            staxStreamReader.next();    // move beyond the end tag.

            handleEndDocument();
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    protected Location getCurrentLocation() {
        return staxStreamReader.getLocation();
    }

    protected String getCurrentQName() {
        return getQName(staxStreamReader.getPrefix(),staxStreamReader.getLocalName());
    }

    private void handleEndElement() throws SAXException {
        processText(false);

        // fire endElement
        tagName.uri = fixNull(staxStreamReader.getNamespaceURI());
        tagName.local = staxStreamReader.getLocalName();
        visitor.endElement(tagName);

        // end namespace bindings
        int nsCount = staxStreamReader.getNamespaceCount();
        for (int i = nsCount - 1; i >= 0; i--) {
            visitor.endPrefixMapping(fixNull(staxStreamReader.getNamespacePrefix(i)));
        }
    }

    private void handleStartElement() throws SAXException {
        processText(true);

        // start namespace bindings
        int nsCount = staxStreamReader.getNamespaceCount();
        for (int i = 0; i < nsCount; i++) {
            visitor.startPrefixMapping(
                fixNull(staxStreamReader.getNamespacePrefix(i)),
                fixNull(staxStreamReader.getNamespaceURI(i)));
        }

        // fire startElement
        tagName.uri = fixNull(staxStreamReader.getNamespaceURI());
        tagName.local = staxStreamReader.getLocalName();
        tagName.atts = attributes;

        visitor.startElement(tagName);
    }

    /**
     * Proxy of {@link Attributes} that read from {@link XMLStreamReader}.
     */
    private final Attributes attributes = new Attributes() {
        public int getLength() {
            return staxStreamReader.getAttributeCount();
        }

        public String getURI(int index) {
            String uri = staxStreamReader.getAttributeNamespace(index);
            if(uri==null)   return "";
            return uri;
        }

        public String getLocalName(int index) {
            return staxStreamReader.getAttributeLocalName(index);
        }

        public String getQName(int index) {
            String prefix = staxStreamReader.getAttributePrefix(index);
            if(prefix==null || prefix.length()==0)
                return getLocalName(index);
            else
                return prefix + ':' + getLocalName(index);
        }

        public String getType(int index) {
            return staxStreamReader.getAttributeType(index);
        }

        public String getValue(int index) {
            return staxStreamReader.getAttributeValue(index);
        }

        public int getIndex(String uri, String localName) {
            for( int i=getLength()-1; i>=0; i-- )
                if( localName.equals(getLocalName(i)) && uri.equals(getURI(i)))
                    return i;
            return -1;
        }

        // this method sholdn't be used that often (if at all)
        // so it's OK to be slow.
        public int getIndex(String qName) {
            for( int i=getLength()-1; i>=0; i-- ) {
                if(qName.equals(getQName(i)))
                    return i;
            }
            return -1;
        }

        public String getType(String uri, String localName) {
            int index = getIndex(uri,localName);
            if(index<0)     return null;
            return getType(index);
        }

        public String getType(String qName) {
            int index = getIndex(qName);
            if(index<0)     return null;
            return getType(index);
        }

        public String getValue(String uri, String localName) {
            int index = getIndex(uri,localName);
            if(index<0)     return null;
            return getValue(index);
        }

        public String getValue(String qName) {
            int index = getIndex(qName);
            if(index<0)     return null;
            return getValue(index);
        }
    };

    protected void handleCharacters() throws XMLStreamException, SAXException {
        if( predictor.expectText() )
            buffer.append(
                staxStreamReader.getTextCharacters(),
                staxStreamReader.getTextStart(),
                staxStreamReader.getTextLength() );
    }

    private void processText( boolean ignorable ) throws SAXException {
        if( predictor.expectText() && (!ignorable || !WhiteSpaceProcessor.isWhiteSpace(buffer))) {
            if(textReported) {
                textReported = false;
            } else {
                visitor.text(buffer);
            }
        }
        buffer.setLength(0);
    }



    /**
     * Reference to FI's StAXReader class, if FI can be loaded.
     */
    private static final Class FI_STAX_READER_CLASS = initFIStAXReaderClass();
    private static final Constructor<? extends StAXConnector> FI_CONNECTOR_CTOR = initFastInfosetConnectorClass();

    private static Class initFIStAXReaderClass() {
        try {
            return UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
        } catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends StAXConnector> initFastInfosetConnectorClass() {
        try {
            Class c = UnmarshallerImpl.class.getClassLoader().loadClass(
                    "com.sun.xml.bind.v2.runtime.unmarshaller.FastInfosetConnector");                
            return c.getConstructor(FI_STAX_READER_CLASS,XmlVisitor.class);
        } catch (Throwable e) {
            return null;
        }
    }

    private static final Class ENHANCED_FI_STAX_READER_CLASS = initEnhancedFIStAXReaderClass();
    private static final Constructor<? extends StAXConnector> ENHANCED_FI_CONNECTOR_CTOR = initEnhancedFastInfosetConnectorClass();

    private static Class initEnhancedFIStAXReaderClass() {
        try {
            return UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.fastinfoset.stax.enhanced.EnhancedStAXDocumentParser");
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Constructor<? extends StAXConnector> initEnhancedFastInfosetConnectorClass() {
        try {
            Class c = UnmarshallerImpl.class.getClassLoader().loadClass(
                    "com.sun.xml.bind.v2.runtime.unmarshaller.EnhancedFastInfosetConnector");
            return c.getConstructor(ENHANCED_FI_STAX_READER_CLASS,XmlVisitor.class);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    //
    // reference to StAXEx classes
    //
    private static final Class STAX_EX_READER_CLASS = initStAXExReader();
    private static final Constructor<? extends StAXConnector> STAX_EX_CONNECTOR_CTOR = initStAXExConnector();

    private static Class initStAXExReader() {
        try {
            return UnmarshallerImpl.class.getClassLoader().loadClass("org.jvnet.staxex.XMLStreamReaderEx");
        } catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends StAXConnector> initStAXExConnector() {
        try {
            Class c = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.bind.v2.runtime.unmarshaller.StAXExConnector");
            return c.getConstructor(STAX_EX_READER_CLASS,XmlVisitor.class);
        } catch (Throwable e) {
            return null;
        }
    }
}
