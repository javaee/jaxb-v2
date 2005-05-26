package com.sun.xml.bind.v2.model.impl;

import javax.xml.namespace.QName;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchema;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.NameConverter;

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


    /**
     * Computes the tag name from a {@link XmlElement} by taking the defaulting into account.
     */
    protected final QName calcXmlName(XmlElement e) {
        if(e!=null)
            return calcXmlName(e.namespace(),e.name());
        else
            return calcXmlName("##default","##default");
    }

    /**
     * Computes the tag name from a {@link XmlElementWrapper} by taking the defaulting into account.
     */
    protected final QName calcXmlName(XmlElementWrapper e) {
        if(e!=null)
            return calcXmlName(e.namespace(),e.name());
        else
            return calcXmlName("##default","##default");
    }

    private final QName calcXmlName(String uri,String local) {
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

    @Override
    protected void link() {
        super.link();
        getXmlName();
    }
}
