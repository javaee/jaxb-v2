package com.sun.xml.bind.v2.model.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

import com.sun.xml.bind.annotation.XmlList;
import com.sun.xml.bind.v2.NameConverter;
import com.sun.xml.bind.v2.TODO;
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
            return getTypes().get(index).getType();
        }

        public int size() {
            return getTypes().size();
        }
    };

    /**
     * Lazily computed.
     * @see #isCollectionNillable()
     */
    private Boolean isNillable;

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

    public final List<TypeRefImpl<TypeT,ClassDeclT>> getTypes() {
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
                TypeT t = _getType();
                if(!nav().isPrimitive(t)) isRequired = false;
                // nillableness defaults to true if it's collection
                types.add(createTypeRef(calcXmlName((XmlElement)null),t,isCollection(),null));
            } else {
                for( XmlElement item : ann ) {
                    // TODO: handle defaulting in names.
                    QName name = calcXmlName(item);
                    TypeT type = reader().getClassValue(item, "type");
                    if(type.equals(nav().ref(XmlElement.DEFAULT.class)))
                        type = _getType();
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
    private TypeRefImpl<TypeT,ClassDeclT> createTypeRef(QName name,TypeT type,boolean isNillable,String defaultValue) {
        return new TypeRefImpl<TypeT,ClassDeclT>(this,name,type,isNillable,defaultValue);
    }

    public boolean isCollectionNillable() {
        if(isNillable==null)
            isNillable = calcCollectionNillable();
        return isNillable;
    }

    public boolean isValueList() {
        if(isValueList==null)
            getTypes(); // compute the value
        return isValueList;
    }

    private boolean calcCollectionNillable() {
        XmlElementWrapper e = seed.readAnnotation(XmlElementWrapper.class);
        if(e==null) return false;
        return e.nillable();
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

    /**
     * Lazily computed. See {@link #getXmlName()}.
     */
    private QName xmlName;
    private boolean xmlNameComputed;

    public final QName getXmlName() {
        if(!isCollection())
            return null;

        if(!xmlNameComputed) {
            xmlNameComputed = true;

            XmlElementWrapper e = seed.readAnnotation(XmlElementWrapper.class);
            if(e!=null)
                xmlName = calcXmlName(e);
            else
                xmlName = null;

            // TODO: we need to remember if the wrapper is nillable or not
        }

        return xmlName;
    }

    protected final QName calcXmlName(XmlElement e) {
        TODO.prototype();       // TODO: handle defaulting
        TODO.checkSpec();       // TODO: name mangling

        if(e!=null)
            return calcXmlName(e.namespace(),e.name());
        else
            return calcXmlName("##default","##default");
    }

    protected final QName calcXmlName(XmlElementWrapper e) {
        TODO.prototype();       // TODO: handle defaulting
        TODO.checkSpec();       // TODO: name mangling

        if(e!=null)
            return calcXmlName(e.namespace(),e.name());
        else
            return calcXmlName("##default","##default");
    }

    protected final QName calcXmlName(String uri,String local) {
        // compute the default
        TODO.checkSpec();
        if(local.length()==0 || local.equals("##default"))
            local = NameConverter.standard.toVariableName(getName());
        if(uri.equals("##default")) {
            XmlSchema xs = reader().getPackageAnnotation( XmlSchema.class, parent.getClazz(), this );
            // JAX-RPC doesn't want the default namespace URI swapping to take effect to
            // local "unqualified" elements. UGLY.
            if(xs!=null) {
                switch(xs.elementFormDefault()) {
                case QUALIFIED:
                    uri = parent.getTypeName().getNamespaceURI();
                    if(uri.length()==0)
                        uri = parent.builder.defaultNsUri;
                    break;
                case UNQUALIFIED:
                case UNSET:
                    uri = "";
                }
            } else {
                uri = "";
            }
        }
        return new QName(uri.intern(),local.intern());
    }

    protected void link() {
        super.link();
        getXmlName();
        for (TypeRefImpl<TypeT, ClassDeclT> ref : getTypes() ) {
            ref.link();
        }
    }
}
