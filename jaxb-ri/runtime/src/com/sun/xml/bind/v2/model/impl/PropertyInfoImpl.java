package com.sun.xml.bind.v2.model.impl;

import java.util.Collection;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * Default partial implementation for {@link PropertyInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class PropertyInfoImpl<TypeT,ClassDeclT,FieldT,MethodT>
    implements PropertyInfo<TypeT,ClassDeclT>, Locatable, Comparable<PropertyInfoImpl> /*by their names*/ {

    /**
     * Object that reads annotations.
     */
    protected final PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> seed;

    /**
     * Lazily computed.
     * @see #isCollection()
     */
    private Boolean isCollection;

    private final ID id;

    private final MimeType expectedMimeType;
    private final boolean inlineBinary;
    private final QName schemaType;

    protected final ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent;

    protected PropertyInfoImpl(ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent, PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> spi) {
        this.seed = spi;
        this.parent = parent;
        this.id = calcId();

        MimeType mt = Util.calcExpectedMediaType(seed,parent.builder);
        if(mt!=null && !kind().canHaveXmlMimeType) {
            parent.builder.reportError(new IllegalAnnotationException(
                Messages.ILLEGAL_ANNOTATION.format(XmlMimeType.class.getName()),
                seed.readAnnotation(XmlMimeType.class)
            ));
            mt = null;
        }
        this.expectedMimeType = mt;
        this.inlineBinary = seed.hasAnnotation(XmlInlineBinaryData.class);
        this.schemaType = Util.calcSchemaType(reader(),seed,parent.clazz,
                nav().asDecl(getIndividualType()),this);
    }


    public ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent() {
        return parent;
    }

    protected final Navigator<TypeT,ClassDeclT,FieldT,MethodT> nav() {
        return parent.nav();
    }
    protected final AnnotationReader<TypeT,ClassDeclT,FieldT,MethodT> reader() {
        return parent.reader();
    }

    public TypeT getRawType() {
        return seed.getRawType();
    }

    public TypeT getIndividualType() {
        TypeT raw = getRawType();
        if(!isCollection()) {
            return raw;
        } else {
            if(nav().isArrayButNotByteArray(raw))
                return nav().getComponentType(raw);

            TypeT bt = nav().getBaseClass(raw, nav().asDecl(Collection.class) );
            if(nav().isParameterizedType(bt))
                return nav().getTypeArgument(bt,0);
            else
                return nav().ref(Object.class);
        }
    }

    public final String getName() {
        return seed.getName();
    }

    public Adapter<TypeT,ClassDeclT> getAdapter() {
        if(seed instanceof AdaptedPropertySeed)
            return ((AdaptedPropertySeed<TypeT,ClassDeclT,FieldT,MethodT>)seed).adapter;
        else
            return null;
    }


    public final String displayName() {
        return nav().getClassName(parent.getClazz())+'#'+getName();
    }

    public final ID id() {
        return id;
    }

    private ID calcId() {
        if(seed.hasAnnotation(XmlID.class)) {
            // check the type
            if(!seed.getRawType().equals(nav().ref(String.class)))
                parent.builder.reportError(new IllegalAnnotationException(
                    Messages.ID_MUST_BE_STRING.format(getName()), seed )
                );
            return ID.ID;
        } else
        if(seed.hasAnnotation(XmlIDREF.class)) {
            return ID.IDREF;
        } else {
            return ID.NONE;
        }
    }

    public final MimeType getExpectedMimeType() {
        return expectedMimeType;
    }

    public final boolean inlineBinaryData() {
        return inlineBinary;
    }

    public QName getSchemaType() {
        return schemaType;
    }

    public final boolean isCollection() {
        if(isCollection==null) {
            TypeT t = seed.getRawType();
            if(nav().isSubClassOf(t,nav().ref(Collection.class))
            || nav().isArrayButNotByteArray(t))
                isCollection = true;
            else
                isCollection = false;
        }
        return isCollection;
    }

    /**
     * Called after all the {@link TypeInfo}s are collected into the governing {@link TypeInfoSet}.
     *
     * Derived class can do additional actions to complete the model.
     */
    protected void link() {
    }

    /**
     * A {@link PropertyInfoImpl} is always referenced by its enclosing class,
     * so return that as the upstream.
     */
    public Locatable getUpstream() {
        return parent;
    }

    public Location getLocation() {
        return seed.getLocation();
    }


//
//
// convenience methods for derived classes
//
//


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
            local = seed.getName();
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

    public int compareTo(PropertyInfoImpl that) {
        return this.getName().compareTo(that.getName());
    }
}
