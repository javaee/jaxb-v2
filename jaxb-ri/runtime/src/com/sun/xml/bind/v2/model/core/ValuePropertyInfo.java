package com.sun.xml.bind.v2.model.core;

/**
 * Value {@link PropertyInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ValuePropertyInfo<TypeT,ClassDeclT> extends PropertyInfo<TypeT,ClassDeclT> {
    /**
     * Gets the type of the value.
     *
     * @return
     *      always non-null.
     */
    NonElement<TypeT,ClassDeclT> getType();
    // TODO: can't we make it a bit more type-safe? it can never be ElementInfo
}
