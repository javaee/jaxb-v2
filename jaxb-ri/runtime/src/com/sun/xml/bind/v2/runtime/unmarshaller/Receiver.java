package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * Receives an object by a child {@link Loader}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Receiver {
    /**
     * Called when the child loader is deactivated.
     *
     * @param state
     *      points to the parent's current state.
     * @param o
     *      object that was loaded. may be null.
     */
    void receive(UnmarshallingContext.State state, Object o) throws SAXException;
}
