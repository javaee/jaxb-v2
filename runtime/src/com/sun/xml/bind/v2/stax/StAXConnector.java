package com.sun.xml.bind.v2.stax;

import javax.xml.stream.XMLStreamException;

/**
 * @author Kohsuke Kawaguchi
 */
public interface StAXConnector {
    void bridge() throws XMLStreamException;
}
