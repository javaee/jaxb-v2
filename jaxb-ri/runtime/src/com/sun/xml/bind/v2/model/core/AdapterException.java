package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Signals an error that the class which is supposed to be an
 * {@link XmlAdapter} is actually not an adapter.
 *
 * @author Kohsuke Kawaguchi
 */
public final class AdapterException extends Exception {
    public AdapterException(String className) {
        super(className);
    }
}
