package com.sun.xml.bind.util;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * {@link SAXParseException} that handles exception chaining correctly.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.0 FCS
 */
public class SAXParseException2 extends SAXParseException {
    public SAXParseException2(String message, Locator locator) {
        super(message, locator);
    }

    public SAXParseException2(String message, Locator locator, Exception e) {
        super(message, locator, e);
    }

    public SAXParseException2(String message, String publicId, String systemId, int lineNumber, int columnNumber) {
        super(message, publicId, systemId, lineNumber, columnNumber);
    }

    public SAXParseException2(String message, String publicId, String systemId, int lineNumber, int columnNumber, Exception e) {
        super(message, publicId, systemId, lineNumber, columnNumber, e);
    }

    public Throwable getCause() {
        return getException();
    }
}
