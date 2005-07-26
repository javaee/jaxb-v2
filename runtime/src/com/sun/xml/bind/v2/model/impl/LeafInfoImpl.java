package com.sun.xml.bind.v2.model.impl;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class LeafInfoImpl<TypeT,ClassDeclT> implements LeafInfo<TypeT,ClassDeclT>, Location {
    private final TypeT type;
    /**
     * Can be null for anonymous types.
     */
    private final QName typeName;

    protected LeafInfoImpl(TypeT type,QName typeName) {
        assert type!=null;

        this.type = type;
        this.typeName = typeName;
    }

    /**
     * A reference to the representation of the type.
     */
    public TypeT getType() {
        return type;
    }

    public QName getTypeName() {
        return typeName;
    }

    public Locatable getUpstream() {
        return null;
    }

    public Location getLocation() {
        // this isn't very accurate, but it's not too bad
        // doing it correctly need leaves to hold navigator.
        // otherwise revisit the design so that we take navigator as a parameter
        return this;
    }

    public String toString() {
        return type.toString();
    }

}
