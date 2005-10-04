package com.sun.xml.bind.v2.runtime.reflect;

import com.sun.xml.bind.api.AccessorException;

/**
 * {@link Accessor} wrapper that replaces a null with an empty collection.
 *
 * <p>
 * This is so that JAX-WS property accessor will work like an ordinary getter.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class NullSafeAccessor<B,V> extends Accessor<B,V> {
    private final Accessor<B,V> core;
    private final Lister lister;

    public NullSafeAccessor(Accessor<B,V> core, Lister lister) {
        super(core.getValueType());
        this.core = core;
        this.lister = lister;
    }

    public V get(B bean) throws AccessorException {
        V v = core.get(bean);
        if(v==null) {
            // creates a new object
            Object pack = lister.startPacking(bean,core);
            lister.endPacking(pack,bean,core);
            v = core.get(bean);
        }
        return v;
    }

    public void set(B bean, V value) throws AccessorException {
        core.set(bean,value);
    }
}
