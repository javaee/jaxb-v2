package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * Template {@link Accessor} for reference types getter/setter.
 *
 * @author Kohsuke Kawaguchi
 */
public class MethodAccessor_Ref extends Accessor {
    public MethodAccessor_Ref() {
        super(Byte.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).get_ref();
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).set_ref((Ref)value);
    }
}
