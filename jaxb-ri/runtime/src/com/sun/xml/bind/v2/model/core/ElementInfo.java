package com.sun.xml.bind.v2.model.core;

import java.util.Collection;

import javax.xml.bind.JAXBElement;

/**
 * A particular use (specialization) of {@link JAXBElement}.
 *
 * TODO: is ElementInfo adaptable?
 *
 * @author Kohsuke Kawaguchi
 */
public interface ElementInfo<TypeT,ClassDeclT> extends Element<TypeT,ClassDeclT> {

    /**
     * Gets the object that represents the value property.
     *
     * @return
     *      non-null.
     */
    ElementPropertyInfo<TypeT,ClassDeclT> getProperty();

    /**
     * Short for <code>getProperty().ref().get(0)</code>.
     *
     * The type of the value this element holds.
     *
     * Normally, this is the T of {@code IXmlElement<T>}.
     * But if the property is adapted, this is the on-the-wire type.
     *
     * Or if the element has a list of values, then this field
     * represents the type of the individual item.
     *
     * @see #getContentInMemoryType()
     */
    NonElement<TypeT,ClassDeclT> getContentType();

    /**
     * T of {@code IXmlElement<T>}.
     *
     * <p>
     * This is tied to the in-memory representation.
     *
     * @see #getContentType()
     */
    TypeT getContentInMemoryType();

    /**
     * Returns the representation for {@link JAXBElement}&lt;<i>contentInMemoryType</i>&gt;.
     *
     * <p>
     * This returns the signature in Java and thus isn't affected by the adapter.
     */
    TypeT getType();

    /**
     * @inheritDoc
     *
     * {@link ElementInfo} can only substitute {@link ElementInfo}. 
     */
    ElementInfo<TypeT,ClassDeclT> getSubstitutionHead();

    /**
     * All the {@link ElementInfo}s whose {@link #getSubstitutionHead()} points
     * to this object.
     *
     * @return
     *      can be empty but never null.
     */
    Collection<? extends ElementInfo<TypeT,ClassDeclT>> getSubstitutionMembers();
}
