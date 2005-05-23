package com.sun.xml.bind.v2.model.impl;

import java.lang.annotation.Annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.PropertyInfo;

/**
 * Exposes the core information that forms a {@link PropertyInfo}.
 */
interface PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> extends Locatable {

    String getName();

    /**
     * Gets the value of the specified annotation from the given property.
     *
     * <p>
     * When this method is used for a property that consists of a getter and setter,
     * it returns the annotation on either of those methods. If both methods have
     * the same annotation, it is an error.
     *
     * @return
     *      null if the annotation is not present.
     */
    <A extends Annotation> A readAnnotation(Class<A> annotationType);

    /**
     * Returns true if the property has the specified annotation.
     * <p>
     * Short for <code>readAnnotation(annotationType)!=null</code>,
     * but this method is typically faster.
     */
    boolean hasAnnotation(Class<? extends Annotation> annotationType);

    /**
     * Gets the actual data type of the field.
     *
     * <p>
     * The data of the property is stored by using this type.
     *
     * <p>
     * The difference between the {@link #getType()} and this method
     * is clear when the property is a multi-value property.
     * The {@link #getType()} method returns the type of the item,
     * but this method returns the actual collection type.
     */
    TypeT getRawType();
}
