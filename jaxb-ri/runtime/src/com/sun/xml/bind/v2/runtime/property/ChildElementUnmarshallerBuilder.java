package com.sun.xml.bind.v2.runtime.property;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.QNameMap;

/**
 * Component that contributes element unmarshallers into
 * {@link ElementDispatcher}.
 *
 * TODO: think of a better name.
 *
 * @author Bhakti Mehta
 */
interface ChildElementUnmarshallerBuilder {
    /**
     * Every Property class has an implementation of buildChildElementUnmarshallers
     * which will fill in the specified {@link QNameMap} by elements that are expected
     * by this property.
     */
    void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<Unmarshaller.Handler> handlers);

    /**
     * Magic {@link QName} used to store a handler for the text.
     *
     * <p>
     * To support the mixed content model, {@link ElementDispatcher} can have
     * at most one {@link Unmarshaller.RawTextHandler} for processing text
     * found amoung elements.
     *
     * This special text handler is put into the {@link QNameMap} parameter
     * of the {@link #buildChildElementUnmarshallers} method by using
     * this magic token as the key.
     */
    public static final QName TEXT_HANDLER = new QName("\u0000","text");

    /**
     * Magic {@link QName} used to store a handler for the rest of the elements.
     *
     * <p>
     * To support the wildcard, {@link ElementDispatcher} can have
     * at most one {@link Unmarshaller.Handler} for processing elements
     * that didn't match any of the named elements.
     *
     * This special text handler is put into the {@link QNameMap} parameter
     * of the {@link #buildChildElementUnmarshallers} method by using
     * this magic token as the key.
     */
    public static final QName CATCH_ALL = new QName("\u0000","catchAll");
}
