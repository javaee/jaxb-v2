package com.sun.xml.bind.v2.runtime.reflect;

import java.io.IOException;

import javax.xml.bind.annotation.XmlValue;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.IDHandler;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.opt.OptimizedTransducedAccessorFactory;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * {@link Accessor} and {@link Transducer} combined into one object.
 *
 * <p>
 * This allows efficient conversions between primitive values and
 * String without using boxing.
 *
 * <p>
 * This abstraction only works for a single-value property.
 *
 * <p>
 * An instance of {@link TransducedAccessor} implicitly holds a
 * field of the {@code BeanT} that the accessors access.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class TransducedAccessor<BeanT> {

    /**
     * @see Transducer#useNamespace()
     */
    public boolean useNamespace() {
        return false;
    }

    /**
     * Obtain the value of the field and declares the namespace URIs used in
     * the value.
     *
     * @see Transducer#declareNamespace(Object, XMLSerializer)
     */
    public void declareNamespace( BeanT o, XMLSerializer w ) throws AccessorException, SAXException {
    }

    /**
     * Prints the responsible field of the given bean to the writer.
     *
     * <p>
     * Use {@link XMLSerializer#getInstance()} to access to the namespace bindings
     */
    public abstract CharSequence print(BeanT o) throws AccessorException, SAXException;

    /**
     * Parses the text value into the responsible field of the given bean.
     *
     * <p>
     * Use {@link UnmarshallingContext#getInstance()} to access to the namespace bindings
     *
     * @throws AccessorException
     *      if the transducer is used to parse an user bean that uses {@link XmlValue},
     *      then this exception may occur when it tries to set the leaf value to the bean.
     * @throws RuntimeException
     *      if the lexical form is incorrect. The method may throw a RuntimeException,
     *      but it shouldn't cause the entire unmarshalling to fail.
     * @throws SAXException
     *      if the parse method found an error, the error is reported, and then
     *      the processing is aborted.
     */
    public abstract void parse(BeanT o, CharSequence lexical) throws AccessorException, SAXException;

    /**
     * Checks if the field has a value.
     */
    public abstract boolean hasValue(BeanT o) throws AccessorException;











    /**
     * Gets the {@link TransducedAccessor} appropriately configured for
     * the given property.
     *
     * <p>
     * This allows the implementation to use an optimized code.
     */
    public static TransducedAccessor get( RuntimePropertyInfo prop, Transducer xducer ) {
        assert !prop.isCollection();
        // TODO: explain why this assertion is true.

        if(prop.id()==ID.ID)
            xducer = new IDHandler.ID(xducer);
        else {
            if(xducer.isDefault()) {
                TransducedAccessor xa = OptimizedTransducedAccessorFactory.get(prop);
                if(xa!=null)    return xa;
            }
        }

        if(xducer.useNamespace())
            return new DefaultContextDependentTransducedAccessorImpl( xducer, prop.getAccessor() );
        else
            return new DefaultTransducedAccessorImpl( xducer, prop.getAccessor() );
    }

    /**
     * Convenience method to write the value as a text inside an element
     * without any attributes.
     * Can be overridden for improved performance.
     */
    public void writeLeafElement(BeanT o, Name tagName, String fieldName, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        assert !useNamespace(); // otherwise the derived class shall override this method
        w.leafElement(tagName,print(o),fieldName);
    }

    static class DefaultContextDependentTransducedAccessorImpl<BeanT> extends DefaultTransducedAccessorImpl<BeanT> {
        public DefaultContextDependentTransducedAccessorImpl(Transducer xducer, Accessor acc) {
            super(xducer, acc);
            assert xducer.useNamespace();
        }

        public boolean useNamespace() {
            return true;
        }

        public void declareNamespace(BeanT bean, XMLSerializer w) throws AccessorException {
            xducer.declareNamespace(acc.get(bean),w);
        }

        @Override
        public void writeLeafElement(BeanT o, Name tagName, String fieldName, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
            w.startElement(tagName,null);
            declareNamespace(o,w);
            w.endNamespaceDecls();
            w.endAttributes();
            w.text(print(o),fieldName);
            w.endElement();
        }
    }


    /**
     * Default implementation of {@link TransducedAccessor} that
     * simply combines a {@link Transducer} and {@link Accessor}.
     */
    static class DefaultTransducedAccessorImpl<BeanT> extends TransducedAccessor<BeanT> {
        protected final Transducer xducer;
        protected final Accessor acc;

        public DefaultTransducedAccessorImpl(Transducer xducer, Accessor acc) {
            this.xducer = xducer;
            this.acc = acc.optimize();
        }

        public CharSequence print(BeanT bean) throws AccessorException {
            return xducer.print(acc.get(bean));
        }

        public void parse(BeanT bean, CharSequence lexical) throws AccessorException, SAXException {
            acc.set(bean,xducer.parse(lexical));
        }

        public boolean hasValue(BeanT bean) throws AccessorException {
            return acc.get(bean)!=null;
        }
    }

    /**
     * Used to recover from errors.
     */
    public static final TransducedAccessor ERROR = new TransducedAccessor() {
        public CharSequence print(Object o) {
            throw new UnsupportedOperationException();
        }

        public void parse(Object o, CharSequence lexical) {
            throw new UnsupportedOperationException();
        }

        public boolean hasValue(Object o) {
            throw new UnsupportedOperationException();
        }
    };
}
