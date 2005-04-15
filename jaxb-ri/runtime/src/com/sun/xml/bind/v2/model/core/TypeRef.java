package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

/**
 * Information about a type referenced from an element property.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeRef<TypeT,ClassDeclT> {
    /**
     * The expected Java object type.
     *
     * The actual instance can be of a subclass of this type.
     *
     * @return
     *      never null.
     */
    NonElement<TypeT,ClassDeclT> getType();

    /**
     * The associated element name.
     *
     * @return
     *      never null.
     */
    QName getTagName();

    /**
     * Returns true if this element is nillable.
     */
    boolean isNillable();

    /**
     * The default value for this element if any.
     * Otherwise null.
     */
    String getDefaultValue();
}
