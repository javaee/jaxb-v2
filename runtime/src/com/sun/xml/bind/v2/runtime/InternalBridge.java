package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.Bridge;

import org.xml.sax.SAXException;

/**
 * Additional methods on {@link Bridge} that are only available for the JAXB runtime.
 * @author Kohsuke Kawaguchi
 */
abstract class InternalBridge<T> extends Bridge<T> {
    protected InternalBridge(JAXBContextImpl context) {
        super(context);
    }

    /**
     * Called from {@link CompositeStructureBeanInfo} to marshal this bridge as a sub-tree.
     */
    abstract void marshal( T o, XMLSerializer out ) throws IOException, SAXException, XMLStreamException;
}
