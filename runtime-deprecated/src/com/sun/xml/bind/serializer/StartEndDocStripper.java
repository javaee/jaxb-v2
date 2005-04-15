package com.sun.xml.bind.serializer;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Removes {@link #startDocument()} and {@link #endDocument()} events
 * from the SAX stream.
 *
 * @since JAXB 2.0
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class StartEndDocStripper extends XMLFilterImpl {
    public StartEndDocStripper(ContentHandler child) {
        setContentHandler(child);
    }

    public void startDocument() {
    }

    public void endDocument() {
    }
}
