package com.sun.xml.bind.v2.model.core;

import javax.xml.namespace.QName;

/**
 * Attribute {@link PropertyInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface AttributePropertyInfo<T,C> extends PropertyInfo<T,C>, NonElementRef<T,C> {
    /**
     * Gets the type of the attribute.
     *
     * <p>
     * Note that when this property is a collection, this method returns
     * the type of each item in the collection.
     *
     * @return
     *      always non-null.
     */
    NonElement<T,C> getTarget();

    /**
     * Returns true if this attribute is mandatory.
     */
    boolean isRequired();

    /**
     * Gets the attribute name.
     *
     * @return
     *      must be non-null.
     */
    QName getXmlName();
}
