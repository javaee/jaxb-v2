package com.sun.xml.bind.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.xml.namespace.QName;

/**
 * A reference to a JAXB-bound type.
 *
 * @since 2.0 EA1
 * @author Kohsuke Kawaguchi
 */
public final class TypeReference {

    /**
     * The associated XML element name that the JAX-RPC uses with this type reference.
     *
     * Always non-null.
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
        this.tagName = tagName;
        this.type = type;
        this.annotations = annotations;

        if(tagName==null || type==null || annotations==null)
            throw new IllegalArgumentException();
    }

    /**
     * Finds the specified annotation from the array and returns it.
     * Null if not found.
     */
    public <A extends Annotation> A get( Class<A> annotationType ) {
        for (Annotation a : annotations) {
            if(a.annotationType()==annotationType)
                return (A)a;
        }
        return null;
    }
}
