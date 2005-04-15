package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * @author Kohsuke Kawaguchi
 */
class AdaptedPropertySeed<TypeT,ClassDeclT,FieldT,MethodT> implements PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> {
    protected final PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> core;

    protected final Adapter<TypeT,ClassDeclT> adapter;

    public AdaptedPropertySeed(PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> core, Adapter<TypeT,ClassDeclT> adapter) {
        this.core = core;
        this.adapter = adapter;
    }

    public String getName() {
        return core.getName();
    }

    public <A extends Annotation> A readAnnotation(Class<A> annotationType) {
        return core.readAnnotation(annotationType);
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return core.hasAnnotation(annotationType);
    }

    public TypeT getRawType() {
        return adapter.defaultType;
    }

    public Location getLocation() {
        return core.getLocation();
    }

    public Locatable getUpstream() {
        return core.getUpstream();
    }

    // those getters/setters should work with the in-memory type.
    // thus they shouldn't invole in the adapting process.
    public String generateSetValue(String $bean, String $var) {
        return core.generateSetValue($bean, $var);
    }

    public String generateGetValue(String $bean) {
        return core.generateGetValue($bean);
    }
}
