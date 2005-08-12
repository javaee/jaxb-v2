package com.sun.xml.bind.v2.runtime;

import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.namespace.QName;

import com.sun.xml.bind.api.AccessorException;

import org.xml.sax.SAXException;

/**
 * {@link Transducer} that signals the runtime that this datatype
 * is marshalled to a different XML Schema type.
 *
 * <p>
 * This transducer is used to implement the semantics of {@link XmlSchemaType} annotation.
 *
 *
 * @see XMLSerializer#schemaType
 * @author Kohsuke Kawaguchi
 */
public class SchemaTypeTransducer<V> implements Transducer<V> {
    private final Transducer<V> core;
    private final QName schemaType;

    public SchemaTypeTransducer(Transducer<V> core, QName schemaType) {
        this.core = core;
        this.schemaType = schemaType;
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
        QName old = w.setSchemaType(schemaType);
        try {
            return core.print(o);
        } finally {
            w.setSchemaType(old);
        }
    }

    public V parse(CharSequence lexical) throws AccessorException, SAXException {
        return core.parse(lexical);
    }
}
