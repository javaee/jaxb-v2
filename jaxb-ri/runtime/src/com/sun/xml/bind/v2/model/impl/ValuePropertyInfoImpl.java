package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;

/**
 * @author Kohsuke Kawaguchi
 */
class ValuePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends SingleTypePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ValuePropertyInfo<TypeT,ClassDeclT> {

    ValuePropertyInfoImpl(
        ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent,
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed) {

        super(parent,seed);
    }

    public PropertyKind kind() {
        return PropertyKind.VALUE;
    }
}
