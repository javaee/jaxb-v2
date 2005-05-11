package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.Attributes;

/**
 * {@link Attributes} extension that allows attribute values
 * to be exposed as {@link CharSequence}.
 *
 * <p>
 * All namespace URIs and local names are assumed to be interned.
 *
 * @author Kohsuke Kawaguchi
 */
public interface AttributesEx extends Attributes {
    /**
     * The same as {@link #getValue(int)}
     */
    CharSequence getData(int idx);

    /**
     * The same as {@link #getValue(String,String)}
     */
    CharSequence getData(String nsUri,String localName);
}
