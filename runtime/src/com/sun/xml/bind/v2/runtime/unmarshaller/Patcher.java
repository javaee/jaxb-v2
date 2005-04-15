package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Patcher {
    void run() throws SAXException;
}
