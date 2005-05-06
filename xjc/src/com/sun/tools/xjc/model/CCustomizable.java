package com.sun.tools.xjc.model;

import com.sun.tools.xjc.Plugin;

import org.xml.sax.Locator;

/**
 * Implemented by model components that can have customizations contributed by {@link Plugin}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CCustomizable {
    /**
     * Gets the list of customizations attached to this model component.
     *
     * @return
     *      can be an empty list but never be null. The returned list is read-only.
     *      Do not modify.
     *
     * @see Plugin#getCustomizationURIs()
     */
    CCustomizations getCustomizations();

    /**
     * Gets the source location in the schema from which this model component is created.
     *
     * @return never null.
     */
    Locator getLocator();
}
