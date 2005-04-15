package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * @author Kohsuke Kawaguchi
 */
class RuntimeReferencePropertyInfoImpl extends ReferencePropertyInfoImpl<Type,Class,Field,Method>
    implements RuntimeReferencePropertyInfo {

    private final Accessor acc;

    public RuntimeReferencePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type,Class,Field,Method> seed) {
        super(classInfo,seed);
        this.acc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
    }

    public Set<? extends RuntimeElement> getElements() {
        return (Set<? extends RuntimeElement>)super.getElements();
    }

    public Set<? extends RuntimeElement> ref() {
        return (Set<? extends RuntimeElement>)super.ref();
    }

    public Type getRawType() {
        return seed.getRawType();
    }

    public Accessor getAccessor() {
        return acc;
    }

    public boolean elementOnlyContent() {
        return !isMixed();
    }
}
