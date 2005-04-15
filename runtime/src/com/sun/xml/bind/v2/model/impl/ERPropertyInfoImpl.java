package com.sun.xml.bind.v2.model.impl;



/**
 * Common part of {@link ElementPropertyInfoImpl} and {@link ReferencePropertyInfoImpl}.
 *
 * TODO: better name!
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ERPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> {

    ERPropertyInfoImpl(
        ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent,
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> propertySeed) {
        super(parent, propertySeed);
    }
}
