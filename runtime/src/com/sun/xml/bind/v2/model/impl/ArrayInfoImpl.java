package com.sun.xml.bind.v2.model.impl;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.runtime.Location;

/**
 *
 * <p>
 * Public because XJC needs to access it
 *
 * @author Kohsuke Kawaguchi
 */
public class ArrayInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends TypeInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ArrayInfo<TypeT,ClassDeclT>, Location {

    private final NonElement<TypeT,ClassDeclT> itemType;

    private final QName typeName;

    /**
     * The representation of T[] in the underlying reflection library.
     */
    private final TypeT arrayType;

    public ArrayInfoImpl(ModelBuilder<TypeT,ClassDeclT,FieldT,MethodT> builder,
                         Locatable upstream, TypeT arrayType) {
        super(builder, upstream);
        this.arrayType = arrayType;
        this.itemType = builder.getTypeInfo(nav().getComponentType(arrayType), this);

        // TODO: check itemType.getTypeName()!=null and report an error
        QName n = itemType.getTypeName();
        this.typeName = calcArrayTypeName(n);
    }

    /**
     * Computes the type name of the array from that of the item type.
     */
    public static final QName calcArrayTypeName(QName n) {
        String uri;
        if(n.getNamespaceURI().equals(WellKnownNamespace.XML_SCHEMA)) {
            TODO.checkSpec("this URI");
            uri = "http://jaxb.dev.java.net/array";
        } else
            uri = n.getNamespaceURI();
        return new QName(uri,n.getLocalPart()+"Array");
    }

    public NonElement<TypeT, ClassDeclT> getItemType() {
        return itemType;
    }

    public QName getTypeName() {
        return typeName;
    }

    public boolean isSimpleType() {
        return false;
    }

    public TypeT getType() {
        return arrayType;
    }

    /**
     * Leaf-type cannot be referenced from IDREF.
     *
     * @deprecated
     *      why are you calling a method whose return value is always known?
     */
    public final boolean canBeReferencedByIDREF() {
        return false;
    }

    public Location getLocation() {
        return this;
    }
    public String toString() {
        return nav().getTypeName(arrayType);
    }
}