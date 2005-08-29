package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.XmlIDREF;

import com.sun.xml.bind.v2.model.annotation.Locatable;


/**
 * Either {@link ClassInfo}, {@link ElementInfo}, or {@link LeafInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeInfo<T,C> extends Locatable {

    /**
     * Gets the underlying Java type that object represents.
     *
     * @return
     *      always non-null.
     */
    T getType();

    /**
     * True if this type is a valid target from a property annotated with {@link XmlIDREF}.
     */
    boolean canBeReferencedByIDREF();
}
