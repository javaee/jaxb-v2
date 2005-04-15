package com.sun.xml.bind.v2.model.core;




/**
 * Either {@link ClassInfo}, {@link ElementInfo}, or {@link LeafInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeInfo<TypeT,ClassDeclT> {

    /**
     * Gets the underlying Java type that object represents.
     *
     * @return
     *      always non-null.
     */
    TypeT getType();
}
