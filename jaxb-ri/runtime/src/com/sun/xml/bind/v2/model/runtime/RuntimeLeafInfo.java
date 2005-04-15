package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.runtime.Transducer;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeLeafInfo extends LeafInfo<Type,Class>, RuntimeNonElement {
    /**
     * {@inheritDoc}
     *
     * @return
     *      always non-null.
     */
    Transducer getTransducer();

    /**
     * The same as {@link #getType()} but returns the type as a {@link Class}.
     * <p>
     * Note that the returned {@link Class} object does not necessarily represents
     * a class declaration. It can be primitive types.
     */
    Class getClazz();
}
