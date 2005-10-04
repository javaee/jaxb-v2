package com.sun.xml.bind.v2.runtime.reflect;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;

/**
 * {@link Accessor} wrapper that replaces a null with an empty collection.
 *
 * <p>
 * This is so that JAX-WS property accessor will work like an ordinary getter.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class NullSafeAccessor<B,V extends Collection> extends Accessor<B,V> {
    private final Accessor<B,V> core;
    private final Class<? extends V> implClass;

    public NullSafeAccessor(Accessor<B,V> core) {
        super(core.getValueType());
        this.core = core;
        this.implClass = ClassFactory.inferImplClass(getValueType(),ClassFactory.COLLECTION_IMPL_CLASSES);
    }

    public V get(B bean) throws AccessorException {
        V v = core.get(bean);
        if(v==null) {
            try {
                v = ClassFactory.create0(implClass);
            } catch (IllegalAccessException e) {
                throw new AccessorException(e);
            } catch (InvocationTargetException e) {
                throw new AccessorException(e);
            } catch (InstantiationException e) {
                throw new AccessorException(e);
            }
            // not sure if we should do this. given the way JAX-WS uses it,
            // probably not.
            // core.set(bean,v);
        }
        return v;
    }

    public void set(B bean, V value) throws AccessorException {
        core.set(bean,value);
    }
}
