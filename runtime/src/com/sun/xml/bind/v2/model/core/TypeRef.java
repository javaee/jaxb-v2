package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

/**
 * Information about a type referenced from {@link ElementPropertyInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeRef<T,C> extends NonElementRef<T,C> {
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
