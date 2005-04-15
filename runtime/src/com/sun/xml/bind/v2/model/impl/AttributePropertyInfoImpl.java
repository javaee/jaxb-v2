package com.sun.xml.bind.v2.model.impl;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyKind;

/**
 * @author Kohsuke Kawaguchi
 */
class AttributePropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements AttributePropertyInfo<TypeT,ClassDeclT> {

    protected final XmlAttribute att;

    /**
     * Lazily computed. See {@link #getXmlName()}.
     */
    private QName xmlName;

    private final boolean isRequired;


    AttributePropertyInfoImpl(ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent, PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed ) {
        super(parent,seed);
        this.att = seed.readAnnotation(XmlAttribute.class);
        assert att!=null;

        if(att.required())
            isRequired = true;
        else {
            isRequired = nav().isPrimitive(_getType());
        }
    }

    public boolean isRequired() {
        return isRequired;
    }

    public final QName getXmlName() {
        if(xmlName==null)
            xmlName = calcXmlName();
        return xmlName;
    }

    private QName calcXmlName() {
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

    public List<? extends NonElement<TypeT,ClassDeclT>> ref() {
        return Collections.singletonList(getType());
    }

    public final PropertyKind kind() {
        return PropertyKind.ATTRIBUTE;
    }

    public NonElement<TypeT,ClassDeclT> getType() {
        return (NonElement<TypeT,ClassDeclT>)super.getType();
    }
}
