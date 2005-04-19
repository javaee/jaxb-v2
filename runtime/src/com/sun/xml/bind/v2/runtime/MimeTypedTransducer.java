package com.sun.xml.bind.v2.runtime;

import java.awt.*;

import javax.activation.MimeType;

import com.sun.xml.bind.api.AccessorException;

import org.xml.sax.SAXException;

/**
 * {@link Transducer} decorator that wraps another {@link Transducer}
 * and sets the expected MIME type to the context.
 *
 * <p>
 * Combined with {@link Transducer} implementations (such as one for {@link Image}),
 * this is used to control the marshalling of the BLOB types.
 *
 * @author Kohsuke Kawaguchi
 */
public final class MimeTypedTransducer<V> implements Transducer<V> {
    private final Transducer<V> core;

    private final MimeType expectedMimeType;

    public MimeTypedTransducer(Transducer<V> core,MimeType expectedMimeType) {
        this.core = core;
        this.expectedMimeType = expectedMimeType;
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
        MimeType old = w.setExpectedMimeType(expectedMimeType);
        try {
            return core.print(o);
        } finally {
            w.setExpectedMimeType(old);
        }
    }

    public V parse(CharSequence lexical) throws AccessorException, SAXException {
        return core.parse(lexical);
    }
}
