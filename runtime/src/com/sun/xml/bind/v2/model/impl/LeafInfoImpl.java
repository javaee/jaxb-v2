package com.sun.xml.bind.v2.model.impl;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.core.LeafInfo;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class LeafInfoImpl<TypeT,ClassDeclT> implements LeafInfo<TypeT,ClassDeclT> {
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

}
