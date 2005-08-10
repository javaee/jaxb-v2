package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.fastinfoset.stax.StAXDocumentParser;

import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;
import org.xml.sax.SAXException;

/**
 * Reads from FastInfoset StAX parser and feeds into JAXB Unmarshaller.
 *
 * @author Paul Sandoz.
 */
final class FastInfosetConnector extends StAXConnector {

    // event source
    private final StAXDocumentParser fastInfosetStreamReader;

    // Flag set to true if there is octets instead of characters
    boolean hasBase64Data = false;
    // Flag set to true if the first chunk of CIIs
    boolean firstCIIChunk = true;

    // Buffer for octets
    private Base64Data base64Data = new Base64Data();

    // Buffer for characters
    private StringBuilder buffer = new StringBuilder();

    public FastInfosetConnector(StAXDocumentParser fastInfosetStreamReader,
            XmlVisitor visitor) {
        super(visitor);
        fastInfosetStreamReader.setStringInterning(true);
        this.fastInfosetStreamReader = fastInfosetStreamReader;
    }

    public void bridge() throws XMLStreamException {
        try {
            // remembers the nest level of elements to know when we are done.
            int depth=0;

            // if the parser is at the start tag, proceed to the first element
            int event = fastInfosetStreamReader.getEventType();
            if(event == XMLStreamConstants.START_DOCUMENT) {
                // nextTag doesn't correctly handle DTDs
                while( !fastInfosetStreamReader.isStartElement() )
                    event = fastInfosetStreamReader.next();
            }
            
                  
            if( event!=XMLStreamConstants.START_ELEMENT)
                throw new IllegalStateException("The current event is not START_ELEMENT\n but " + event);
            
            handleStartDocument();

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
                
                event=fastInfosetStreamReader.next();
            }

            fastInfosetStreamReader.next();    // move beyond the end tag.

            handleEndDocument();
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    protected Location getCurrentLocation() {
        return fastInfosetStreamReader.getLocation();
    }

    protected String getCurrentQName() {
        return fastInfosetStreamReader.getNameString();
    }

    private void handleStartElement() throws SAXException {
        processText(true);

        for (int i = 0; i < fastInfosetStreamReader.getNamespaceCount(); i++) {
            visitor.startPrefixMapping(fastInfosetStreamReader.getNamespacePrefix(i),
                    fastInfosetStreamReader.getNamespaceURI(i));
        }

        tagName.uri = fastInfosetStreamReader.getNamespaceURI();
        tagName.local = fastInfosetStreamReader.getLocalName();
        tagName.atts = fastInfosetStreamReader.getAttributesHolder();

        visitor.startElement(tagName);
    }

    private void handleCharacters() {
        if (context.expectText()) {
            // If the first chunk of CIIs and character data is present
            if (firstCIIChunk &&
                    fastInfosetStreamReader.getTextAlgorithmBytes() == null) {
                buffer.append(fastInfosetStreamReader.getTextCharacters(),
                        fastInfosetStreamReader.getTextStart(),
                        fastInfosetStreamReader.getTextLength());
                firstCIIChunk = false;
            // If the first chunk of CIIs and octet data is present
            } else if (firstCIIChunk &&
                    fastInfosetStreamReader.getTextAlgorithmIndex() == EncodingAlgorithmIndexes.BASE64) {
                firstCIIChunk = false;
                hasBase64Data = true;
                // Clone the octets
                base64Data.set(fastInfosetStreamReader.getTextAlgorithmBytesClone(), "");
                return;
            // If a subsequent sequential chunk of CIIs
            } else {
                // If the first chunk is octet data
                if (hasBase64Data) {
                    // Append base64 encoded octets to the character buffer
                    buffer.append(base64Data);
                    hasBase64Data = false;
                }

                // Append the second or subsequence chunk of CIIs to the buffer
                buffer.append(fastInfosetStreamReader.getTextCharacters(),
                        fastInfosetStreamReader.getTextStart(),
                        fastInfosetStreamReader.getTextLength());
            }

        }
    }

    private void handleEndElement() throws SAXException {
        processText(false);

        tagName.uri = fastInfosetStreamReader.getNamespaceURI();
        tagName.local = fastInfosetStreamReader.getLocalName();

        visitor.endElement(tagName);

        for (int i = fastInfosetStreamReader.getNamespaceCount() - 1; i >= 0; i--) {
            visitor.endPrefixMapping(fastInfosetStreamReader.getNamespacePrefix(i));
        }
    }

    private void processText(boolean ignorable) throws SAXException {
        if (firstCIIChunk == true) {
            return;
        }
        firstCIIChunk = true;

        // If there are characters
        if (buffer.length() > 0) {
            if (!ignorable || !WhiteSpaceProcessor.isWhiteSpace(buffer)) {
                visitor.text(buffer);
            }

            // avoid excessive object allocation, but also avoid
            // keeping a huge array inside StringBuffer.
            if (buffer.length()<1024) {
                buffer.setLength(0);
            } else {
                buffer = new StringBuilder();
            }
        // If there are octets
        } else if (hasBase64Data) {
            visitor.text(base64Data);
            hasBase64Data = false;
        }
    }
}

