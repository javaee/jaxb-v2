package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.util.AttributesImpl;

/**
 * {@link AttributesEx} implementation.
 * 
 * TODO: proper implementation that holds CharSequence
 *
 * @author Kohsuke Kawaguchi
 */
public final class AttributesExImpl extends AttributesImpl implements AttributesEx {
    public CharSequence getData(int idx) {
        return getValue(idx);
    }

    public CharSequence getData(String nsUri, String localName) {
        return getData(nsUri,localName);
    }
}
