package com.sun.xml.bind.v2.model.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlList;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;

/**
 * Common {@link ElementPropertyInfo} implementation used for both
 * APT and runtime.
 * 
 * @author Kohsuke Kawaguchi
 */
class ElementPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    extends ERPropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements ElementPropertyInfo<TypeT,ClassDeclT>
{
    /**
     * Lazily computed.
     * @see #getTypes()
     */
    private List<TypeRefImpl<TypeT,ClassDeclT>> types;

    private final List<TypeInfo<TypeT,ClassDeclT>> ref = new AbstractList<TypeInfo<TypeT,ClassDeclT>>() {
        public TypeInfo<TypeT,ClassDeclT> get(int index) {
            return getTypes().get(index).getTarget();
        }

        public int size() {
            return getTypes().size();
        }
    };

    /**
     * Lazily computed.
     * @see #isRequired()
     */
    private Boolean isRequired;

    /**
     * Lazily computed.
     * @see #isValueList()
     */
    private Boolean isValueList;

    ElementPropertyInfoImpl(
        ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent,
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> propertySeed) {
        super(parent, propertySeed);
    }

    public List<? extends TypeRefImpl<TypeT,ClassDeclT>> getTypes() {
        if(types==null) {
            types = new ArrayList<TypeRefImpl<TypeT,ClassDeclT>>();
            XmlElement[] ann=null;

            XmlElement xe = seed.readAnnotation(XmlElement.class);
            XmlElements xes = seed.readAnnotation(XmlElements.class);

            if(xe!=null && xes!=null) {
                // TODO: report an error
            }

            isRequired = true;

            isValueList = seed.hasAnnotation(XmlList.class);

            if(xe!=null)
                ann = new XmlElement[]{xe};
            else
            if(xes!=null)
                ann = xes.value();

            if(ann==null) {
                // default
                //TODO check with spec later
                TypeT t = getIndividualType();
                if(!nav().isPrimitive(t)) isRequired = false;
                // nillableness defaults to true if it's collection
                types.add(createTypeRef(calcXmlName((XmlElement)null),t,isCollection(),null));
            } else {
                for( XmlElement item : ann ) {
                    // TODO: handle defaulting in names.
                    QName name = calcXmlName(item);
                    TypeT type = reader().getClassValue(item, "type");
                    if(type.equals(nav().ref(XmlElement.DEFAULT.class))) type = getIndividualType();
                    if(!nav().isPrimitive(type)) isRequired = false;
                    types.add(createTypeRef(name, type, item.nillable(), getDefaultValue(item.defaultValue()) ));
                }
            }
            types = Collections.unmodifiableList(types);
            assert !types.contains(null);
        }
        return types;
    }

    private String getDefaultValue(String value) {
        if(value.equals("\u0000"))
            return null;
        else
            return value;
    }

    /**
     * Used by {@link PropertyInfoImpl} to create new instances of {@link TypeRef}
     */
    protected TypeRefImpl<TypeT,ClassDeclT> createTypeRef(QName name,TypeT type,boolean isNillable,String defaultValue) {
        return new TypeRefImpl<TypeT,ClassDeclT>(this,name,type,isNillable,defaultValue);
    }

    public boolean isValueList() {
        if(isValueList==null)
            getTypes(); // compute the value
        return isValueList;
    }

    public boolean isRequired() {
        if(isRequired==null)
            getTypes(); // compute the value
        return isRequired;
    }

    public List<? extends TypeInfo<TypeT,ClassDeclT>> ref() {
        return ref;
    }

    public final PropertyKind kind() {
        return PropertyKind.ELEMENT;
    }

    protected void link() {
        super.link();
        for (TypeRefImpl<TypeT, ClassDeclT> ref : getTypes() ) {
            ref.link();
        }
    }
}
