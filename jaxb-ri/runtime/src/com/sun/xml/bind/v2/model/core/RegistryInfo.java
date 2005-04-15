package com.sun.xml.bind.v2.model.core;

import java.util.Set;

import javax.xml.bind.annotation.XmlRegistry;

import com.sun.xml.bind.v2.model.impl.ModelBuilder;

/**
 * Represents the information in a class with {@link XmlRegistry} annotaion.
 *
 * <p>
 * This interface is only meant to be used as a return type from {@link ModelBuilder}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface RegistryInfo<T,C> {
    /**
     * Returns all the references to other types in this registry.
     */
    Set<TypeInfo<T,C>> getReferences();
}
