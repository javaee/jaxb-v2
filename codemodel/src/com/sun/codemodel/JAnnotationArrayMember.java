/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved. SUN
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.annotation.Annotation;

/**
 * Represents an arrays as annotation members
 *
 * <p>
 * This class implements {@link JAnnotatable} to allow
 * new annotations to be added as a member of the array.
 *
 * @author
 *     Bhakti Mehta (bhakti.mehta@sun.com)
 */
public final class JAnnotationArrayMember extends JAnnotationValue implements JAnnotatable {
    private final List values = new ArrayList();
    private final JCodeModel owner;

    JAnnotationArrayMember(JCodeModel owner) {
        this.owner = owner;
    }

    /**
     * Adds an array member to this annotation
     *
     * @param value Adds a string value to the array member
     * @return The JAnnotationArrayMember. More elements can be added by calling
     *         the same method multiple times
     */
    public JAnnotationArrayMember param(String value) {
        JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
        values.add(annotationValue);
        return this;
    }

    public JAnnotationArrayMember param(boolean value) {
        JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
        values.add(annotationValue);
        return this;
    }

    /**
     * Adds an array member to this annotation
     *
     * @param value Adds an int value to the array member
     * @return The JAnnotationArrayMember. More elements can be added by calling
     *         the same method multiple times
     */
    public JAnnotationArrayMember param(int value) {
        JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
        values.add(annotationValue);
        return this;
    }

    /**
     * Adds an array member to this annotation
     *
     * @param value Adds a float value to the array member
     * @return The JAnnotationArrayMember. More elements can be added by calling
     *         the same method multiple times
     */
    public JAnnotationArrayMember param(float value) {
        JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
        values.add(annotationValue);
        return this;
    }

    public JAnnotationArrayMember param(Class value){
       JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value.getName()));
       values.add(annotationValue);
       return this;
   }

    public JAnnotationArrayMember param(JType type){
        JClass clazz = type.boxify();
        JAnnotationValue annotationValue = new JAnnotationStringValue ( clazz.dotclass() );
        values.add(annotationValue);
        return this;
    }

    /**
     * Adds a new annotation to the array.
     */
    public JAnnotationUse annotate(Class<? extends Annotation> clazz){
        return annotate(owner.ref(clazz));
    }

    /**
     * Adds a new annotation to the array.
     */
    public JAnnotationUse annotate(JClass clazz){
        JAnnotationUse a = new JAnnotationUse(clazz);
        values.add(a);
        return a;
    }

    public <W extends JAnnotationWriter> W annotate2(Class<W> clazz) {
        return TypedAnnotationWriter.create(clazz,this);
    }

    /**
     * Adds an annotation member to this annotation  array
     * This can be used for e.g &#64;XmlCollection(values= &#64;XmlCollectionItem(type=Foo.class))
     * @param value
     *        Adds a annotation  to the array member
     * @return
     *        The JAnnotationArrayMember. More elements can be added by calling
     *        the same method multiple times
     *
     * @deprecated
     *      use {@link #annotate}
     */
    public JAnnotationArrayMember param (JAnnotationUse value ){
        values.add(value);
        return this;
    }

    public void generate(JFormatter f) {
        f.p('{').nl().i();

        boolean first = true;
        for (Iterator i = values.iterator(); i.hasNext();) {
            if (!first)
                f.p(',').nl();

            JAnnotationValue aValue = (JAnnotationValue) i.next();
            f.g(aValue);
            first = false;
        }
        f.nl().o().p('}');
    }
}

