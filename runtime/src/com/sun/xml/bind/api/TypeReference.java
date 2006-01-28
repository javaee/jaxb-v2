package com.sun.xml.bind.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Collection;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * A reference to a JAXB-bound type.
 *
 * <p>
 * <b>Subject to change without notice</b>.
 *
 * @since 2.0 EA1
 * @author Kohsuke Kawaguchi
 */
public final class TypeReference {

    /**
     * The associated XML element name that the JAX-RPC uses with this type reference.
     *
     * Always non-null. Strings are interned.
     */
    public final QName tagName;

    /**
     * The Java type that's being referenced.
     *
     * Always non-null.
     */
    public final Type type;

    /**
     * The annotations associated with the reference of this type.
     *
     * Always non-null.
     */
    public final Annotation[] annotations;

    public TypeReference(QName tagName, Type type, Annotation... annotations) {
        if(tagName==null || type==null || annotations==null)
            throw new IllegalArgumentException();

        this.tagName = new QName(tagName.getNamespaceURI().intern(), tagName.getLocalPart().intern(), tagName.getPrefix());
        this.type = type;
        this.annotations = annotations;
    }

    /**
     * Finds the specified annotation from the array and returns it.
     * Null if not found.
     */
    public <A extends Annotation> A get( Class<A> annotationType ) {
        for (Annotation a : annotations) {
            if(a.annotationType()==annotationType)
                return annotationType.cast(a);
        }
        return null;
    }

    /**
     * Creates a {@link TypeReference} for the item type,
     * if this {@link TypeReference} represents a collection type.
     * Otherwise returns an identical type.
     */
    public TypeReference toItemType() {
        assert annotations.length==0;   // not designed to work with adapters.

        Type base = Navigator.REFLECTION.getBaseClass(type, Collection.class);
        if(base==null)
            return this;    // not a collection

        return new TypeReference(tagName,
            Navigator.REFLECTION.getTypeArgument(base,0));
    }
}
