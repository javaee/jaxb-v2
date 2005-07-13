package com.sun.xml.bind.v2.model.impl;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.namespace.QName;

/**
 * Common part of {@link ElementPropertyInfoImpl} and {@link ReferencePropertyInfoImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ERPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> {

    public ERPropertyInfoImpl(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> classInfo, PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> propertySeed) {
        super(classInfo, propertySeed);

        boolean nil = false;
        if(!isCollection())
            xmlName = null;
        else {
            XmlElementWrapper e = seed.readAnnotation(XmlElementWrapper.class);
            if(e!=null) {
                xmlName = calcXmlName(e);
                nil = e.nillable();
            } else
                xmlName = null;
        }

        wrapperNillable = nil;
    }

    private final QName xmlName;

    /**
     * True if the wrapper tag name is nillable.
     */
    private final boolean wrapperNillable;

    /**
     * Gets the wrapper element name.
     */
    public final QName getXmlName() {
        return xmlName;
    }

    public final boolean isCollectionNillable() {
        return wrapperNillable;
    }
}
