package com.sun.xml.bind.v2.runtime;

import javax.xml.bind.annotation.XmlValue;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.opt.OptimizedTransducedAccessorFactory;

import org.xml.sax.SAXException;


/**
 * Responsible for converting a Java object to a lexical representation
 * and vice versa.
 *
 * <p>
 * An implementation of this interface hides how this conversion happens.
 *
 * <p>
 * {@link Transducer}s are immutable.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface Transducer<ValueT> {

    /**
     * If this {@link Transducer} is the default transducer for the <code>ValueT</code>,
     * this method returns true.
     *
     * Used exclusively by {@link OptimizedTransducedAccessorFactory#get(RuntimePropertyInfo)}
     */
    boolean isDefault();

    /**
     * If true, this {@link Transducer} doesn't declare any namespace,
     * and therefore {@link #declareNamespace(Object, XMLSerializer)} is no-op.
     *
     * It also means that the {@link #parse(CharSequence)} method
     * won't use the context parameter.
     */
    boolean useNamespace();

    /**
     * Declares the namespace URIs used in the given value to {@code w}.
     *
     * @param o
     *      never be null.
     * @param w
     *      may be null if {@code !{@link #useNamespace()}}.
     */
    void declareNamespace( ValueT o, XMLSerializer w ) throws AccessorException;

    /**
     * Converts the given value to its lexical representation.
     *
     * @param o
     *      never be null.
     * @return
     *      always non-null valid lexical representation.
     */
    CharSequence print(ValueT o) throws AccessorException;

    /**
     * Converts the lexical representation to a value object.
     *
     * @param lexical
     *      never be null.
     * @throws AccessorException
     *      if the transducer is used to parse an user bean that uses {@link XmlValue},
     *      then this exception may occur when it tries to set the leaf value to the bean.
     * @throws SAXException
     *      if the lexical form is incorrect, the error should be reported
     *      and SAXException may thrown (or it can return null to recover.)
     */
    ValueT parse(CharSequence lexical) throws AccessorException, SAXException;
}
