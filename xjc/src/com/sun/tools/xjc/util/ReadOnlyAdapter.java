package com.sun.tools.xjc.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link XmlAdapter} used inside XJC is almost always unmarshal-only.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ReadOnlyAdapter<OnTheWire,InMemory> extends XmlAdapter<OnTheWire,InMemory> {
    public final OnTheWire marshal(InMemory onTheWire) {
        throw new UnsupportedOperationException();
    }
}
