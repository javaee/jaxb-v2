package com.sun.xml.bind.v2.runtime;

import javax.activation.MimeType;

import com.sun.xml.bind.api.AccessorException;

import org.xml.sax.SAXException;

/**
 * Transducer that signals the runtime that this binary data shall be always inlined.
 *
 * @author Kohsuke Kawaguchi
 */
public class InlineBinaryTransducer<V> implements Transducer<V> {
    private final Transducer<V> core;

    public InlineBinaryTransducer(Transducer<V> core) {
        this.core = core;
    }

    public boolean isDefault() {
        return false;
    }

    public boolean useNamespace() {
        return core.useNamespace();
    }

    public void declareNamespace( V o, XMLSerializer w ) throws AccessorException {
        core.declareNamespace(o, w);
    }

    public CharSequence print(V o) throws AccessorException {
        XMLSerializer w = XMLSerializer.getInstance();
        boolean old = w.setInlineBinaryFlag(true);
        try {
            return core.print(o);
        } finally {
            w.setInlineBinaryFlag(old);
        }
    }

    public V parse(CharSequence lexical) throws AccessorException, SAXException {
        return core.parse(lexical);
    }
}
