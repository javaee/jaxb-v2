package com.sun.tools.xjc.reader.dtd.bindinfo;

import javax.xml.parsers.ParserConfigurationException;

import com.sun.xml.bind.marshaller.SAX2DOMEx;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi
 */
final class DOMBuilder extends SAX2DOMEx {
    private Locator locator;

    public DOMBuilder() throws ParserConfigurationException {
    }

    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    public void startElement(String namespace, String localName, String qName, Attributes attrs) {
        super.startElement(namespace, localName, qName, attrs);
        DOM4JLocator.setLocationInfo(getCurrentElement(),locator);
    }
}
