package org.kohsuke.rngom.xml.sax;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * {@link XMLReaderCreator} that uses JAXP to create
 * {@link XMLReader}s.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class JAXPXMLReaderCreator implements XMLReaderCreator {

    private final SAXParserFactory spf;
    
    public JAXPXMLReaderCreator( SAXParserFactory spf ) {
        this.spf = spf;
    }
    
    /**
     * Creates a {@link JAXPXMLReaderCreator} by using
     * {@link SAXParserFactory#newInstance()}.
     */
    public JAXPXMLReaderCreator() {
        spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
    }
    
    /**
     * @see org.kohsuke.rngom.xml.sax.XMLReaderCreator#createXMLReader()
     */
    public XMLReader createXMLReader() throws SAXException {
        try {
            return spf.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }

}
