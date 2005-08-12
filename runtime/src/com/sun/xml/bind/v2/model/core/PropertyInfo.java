package com.sun.xml.bind.v2.model.core;

import java.util.Collection;

import javax.activation.MimeType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.namespace.QName;

/**
 * Information about a JAXB-bound property.
 *
 * <p>
 * All the JAXB annotations are already incorporated into the model so that
 * the caller doesn't have to worry about reading them. For this reason, you
 * cannot access annotations on properties directly.
 *
 * @author Kohsuke Kawaguchi
 */
public interface PropertyInfo<TypeT,ClassDeclT> {

    /**
     * Gets the {@link ClassInfo} or {@link ElementInfo} to which this property belongs.
     */
    TypeInfo<TypeT,ClassDeclT> parent();

    /**
     * Gets the name of the property.
     *
     * <p>
     * For example, "foo" or "bar". <b>This doesn't directly affect XML</b>.
     * The property name uniquely identifies a property within a class.
     *
     * @see XmlType#propOrder()
     */
    String getName();

    /**
     * Gets the display name of the property.
     *
     * <p>
     * This is a convenience method for
     * {@code parent().getName()+'#'+getName()}.
     */
    String displayName();

    /**
     * Returns true if this is a multi-valued collection property.
     * Otherwise false, in which case the property is a single value.
     */
    boolean isCollection();

    /**
     * List of {@link TypeInfo}s that this property references.
     *
     * This allows the caller to traverse the reference graph without
     * getting into the details of each different property type.
     *
     * @return
     *      non-null read-only collection.
     */
    Collection<? extends TypeInfo<TypeT,ClassDeclT>> ref();

    /**
     * Gets the kind of this proeprty.
     *
     * @return
     *      always non-null.
     */
    PropertyKind kind();

    /**
     * @return
     *      null if the property is not adapted.
     */
    Adapter<TypeT,ClassDeclT> getAdapter();

    /**
     * Returns the IDness of the value of this element.
     *
     * @see XmlID
     * @see XmlIDREF
     *
     * @return
     *      always non-null
     */
    ID id();

    /**
     * Expected MIME type, if any.
     */
    MimeType getExpectedMimeType();

    /**
     * If this is true and this property indeed represents a binary data,
     * it should be always inlined.
     */
    boolean inlineBinaryData();

    /**
     * The effective value of {@link XmlSchemaType} annotation, if any.
     *
     * @return maye be null.
     */
    QName getSchemaType();
}
