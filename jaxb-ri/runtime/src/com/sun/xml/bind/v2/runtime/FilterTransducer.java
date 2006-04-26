package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;

import org.xml.sax.SAXException;

/**
 * {@link Transducer} that delegates to another {@link Transducer}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class FilterTransducer<T> implements Transducer<T> {
    protected final Transducer<T> core;

    protected FilterTransducer(Transducer<T> core) {
        this.core = core;
    }

    public final boolean isDefault() {
        // this must be used as a decorating transducer,
        // so it may never be default.
        return false;
    }

    public boolean useNamespace() {
        return core.useNamespace();
    }

    public void declareNamespace(T o, XMLSerializer w) throws AccessorException {
        core.declareNamespace(o, w);
    }

    public CharSequence print(T o) throws AccessorException {
        return core.print(o);
    }

    public T parse(CharSequence lexical) throws AccessorException, SAXException {
        return core.parse(lexical);
    }

    public void writeText(XMLSerializer w, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        core.writeText(w, o, fieldName);
    }

    public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        core.writeLeafElement(w,tagName,o,fieldName);
    }

    public QName getTypeName(T instance) {
        return null;
    }
}
