package com.sun.xml.bind.v2.model.core;



/**
 * Reference to a {@link NonElement}.
 *
 * This interface defines properties of a reference.
 *
 * @author Kohsuke Kawaguchi
 */
public interface NonElementRef<T,C> {
    /**
     * Target of the reference.
     *
     * @return never null
     */
    NonElement<T,C> getTarget();

    /**
     * Gets the property which is the source of this reference.
     *
     * @return never null
     */
    PropertyInfo<T,C> getSource();
}
