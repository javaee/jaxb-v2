package com.sun.xml.bind.v2.model.impl;

import java.util.Collection;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlMimeType;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
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
    implements PropertyInfo<TypeT,ClassDeclT>, Locatable {

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

    protected final ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent;

    protected PropertyInfoImpl(ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent, PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> spi) {
        this.seed = spi;
        this.parent = parent;
        this.id = calcId();

        XmlMimeType xmt = seed.readAnnotation(XmlMimeType.class);
        MimeType mt = null;
        if(xmt!=null) {
            if(!kind().canHaveXmlMimeType) {
                parent.builder.reportError(new IllegalAnnotationException(
                    Messages.ILLEGAL_ANNOTATION.format(XmlMimeType.class.getName()),
                    xmt
                ));
            } else {
                try {
                    mt = new MimeType(xmt.value());
                } catch (MimeTypeParseException e) {
                    parent.builder.reportError(new IllegalAnnotationException(
                        Messages.ILLEGAL_MIME_TYPE.format(xmt.value(),e.getMessage()),
                        xmt
                    ));
                }
            }
        }
        this.expectedMimeType = mt; 
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
        XmlID ida = seed.readAnnotation(XmlID.class);
        if(ida!=null) {
            return ID.ID;
        } else
        if(seed.readAnnotation(XmlIDREF.class)!=null) {
            return ID.IDREF;
        } else {
            return ID.NONE;
        }
    }

    public final MimeType getExpectedMimeType() {
        return expectedMimeType;
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
}
