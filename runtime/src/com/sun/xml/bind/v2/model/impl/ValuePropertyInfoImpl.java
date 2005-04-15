package com.sun.xml.bind.v2.model.impl;

import java.util.Collections;
import java.util.List;

import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;

/**
 * @author Kohsuke Kawaguchi
 */
class ValuePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ValuePropertyInfo<TypeT,ClassDeclT> {

    ValuePropertyInfoImpl(
        ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent,
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed) {

        super(parent,seed);
    }

    public List<? extends NonElement<TypeT,ClassDeclT>> ref() {
        return Collections.singletonList(getType());
    }

    public PropertyKind kind() {
        return PropertyKind.VALUE;
    }

    public NonElement<TypeT,ClassDeclT> getType() {
        return (NonElement<TypeT,ClassDeclT>)super.getType();
    }
}
