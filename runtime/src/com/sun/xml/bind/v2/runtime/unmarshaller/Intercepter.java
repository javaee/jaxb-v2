package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.SAXException;

/**
 * Used solely by {@link com.sun.xml.bind.v2.runtime.ElementBeanInfoImpl} to wrap
 * the loaded value object into a JAXBElement object.
 *
 * UGLY HACK.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Intercepter {
    /**
     * Called when the child loader is deactivated.
     *
     * @param state
     *      points to the parent's current state.
     * @param o
     *      object that was loaded. may be null.
     */
    Object intercept(UnmarshallingContext.State state, Object o) throws SAXException;
}
