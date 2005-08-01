package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.v2.stax.StAXConnector;
import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Reads from FastInfoset StAX parser and feeds into JAXB Unmarshaller.
 *
 * @author Paul Sandoz.
 */
public class FastInfosetConnector implements StAXConnector {

    // event source
    private final StAXDocumentParser fastInfosetStreamReader;

    // event sink
    private final XmlVisitor visitor;

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
        fastInfosetStreamReader.setStringInterning(true);
        this.fastInfosetStreamReader = fastInfosetStreamReader;
        this.visitor = visitor;
    }

    public void bridge() throws XMLStreamException {
        try {
            handleStartDocument();

            while (fastInfosetStreamReader.hasNext()) {
                final int event = fastInfosetStreamReader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT :
                        handleStartElement();
                        break;
                    case XMLStreamConstants.END_ELEMENT :
                        handleEndElement();
                        break;
                    case XMLStreamConstants.CHARACTERS :
                        handleCharacters();
                        break;
                }
            }

            handleEndDocument();
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleEndDocument() throws SAXException {
        visitor.endDocument();
    }

    private void handleStartDocument() throws SAXException {
        final Locator locator = new Locator() {
            public int getColumnNumber() {
                return fastInfosetStreamReader.getLocation().getColumnNumber();
            }
            public int getLineNumber() {
                return fastInfosetStreamReader.getLocation().getLineNumber();
            }
            public String getPublicId() {
                return fastInfosetStreamReader.getLocation().getPublicId();
            }
            public String getSystemId() {
                return fastInfosetStreamReader.getLocation().getSystemId();
            }
        };

        visitor.startDocument(new LocatorEx() {
            public ValidationEventLocator getLocation() {
                return new ValidationEventLocatorImpl(locator);
            }
            public String getPublicId() {
                return locator.getPublicId();
            }
            public String getSystemId() {
                return locator.getSystemId();
            }
            public int getLineNumber() {
                return locator.getLineNumber();
            }
            public int getColumnNumber() {
                return locator.getColumnNumber();
            }
        });
    }

    private void handleStartElement() throws XMLStreamException {
        try {
            processText(true);

            for (int i = 0; i < fastInfosetStreamReader.getNamespaceCount(); i++) {
                visitor.startPrefixMapping(fastInfosetStreamReader.getNamespacePrefix(i),
                        fastInfosetStreamReader.getNamespaceURI(i));
            }

            visitor.startElement(
                    fastInfosetStreamReader.getNamespaceURI(),
                    fastInfosetStreamReader.getLocalName(),
                    fastInfosetStreamReader.getNameString(),
                    fastInfosetStreamReader.getAttributesHolder());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleCharacters() throws XMLStreamException {
        if (visitor.expectText()) {
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

    private void handleEndElement() throws XMLStreamException {
        try {
            processText(false);

            visitor.endElement(
                    fastInfosetStreamReader.getNamespaceURI(),
                    fastInfosetStreamReader.getLocalName(),
                    fastInfosetStreamReader.getNameString());

            for (int i = fastInfosetStreamReader.getNamespaceCount() - 1; i >= 0; i--) {
                visitor.endPrefixMapping(fastInfosetStreamReader.getNamespacePrefix(i));
            }
        } catch (SAXException e) {
            throw new XMLStreamException(e);
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

