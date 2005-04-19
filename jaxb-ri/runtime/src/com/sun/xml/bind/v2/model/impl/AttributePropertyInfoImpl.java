package com.sun.xml.bind.v2.model.impl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;

/**
 * @author Kohsuke Kawaguchi
 */
class AttributePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends SingleTypePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements AttributePropertyInfo<TypeT,ClassDeclT> {

    private final QName xmlName;

    private final boolean isRequired;

    AttributePropertyInfoImpl(ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent, PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed ) {
        super(parent,seed);
        XmlAttribute att = seed.readAnnotation(XmlAttribute.class);
        assert att!=null;

        if(att.required())
            isRequired = true;
        else
            isRequired = nav().isPrimitive(_getType());

        this.xmlName = calcXmlName(att);
    }

    private QName calcXmlName(XmlAttribute att) {
        TODO.prototype();       // TODO: handle defaulting
        TODO.checkSpec();       // TODO: name mangling

        String uri;
        String local;

        TODO.checkSpec();
        uri = att.namespace();
        local = att.name();

        // compute the default
        TODO.checkSpec();
        if(local.equals("##default"))
            local = NameConverter.standard.toVariableName(getName());
        if(uri.equals("##default"))
            uri = "";

        return new QName(uri.intern(),local.intern());
    }

    public boolean isRequired() {
        return isRequired;
    }

    public final QName getXmlName() {
        return xmlName;
    }

    public final PropertyKind kind() {
        return PropertyKind.ATTRIBUTE;
    }
}
