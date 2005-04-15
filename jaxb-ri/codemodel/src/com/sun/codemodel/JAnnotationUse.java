/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved. SUN
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;


import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents an annotation on a program element.
 *
 * TODO
 *    How to add enums to the annotations
 * @author
 *     Bhakti Mehta (bhakti.mehta@sun.com)
 */
public final class JAnnotationUse extends JAnnotationValue {

    /**
     * The {@link Annotation} class
     */
    private final JClass clazz;

    /**
     * Map of member values.
     *
     * <p>
     * Use ordered map to keep the code generation the same on any JVM.
     */
    private final SortedMap<String,JAnnotationValue> memberValues = new TreeMap<String,JAnnotationValue>();

    JAnnotationUse(JClass clazz){
        this.clazz = clazz;
    }

    private JCodeModel owner() {
        return clazz.owner();
    }

    /**
     * Adds a member value pair to this annotation
     *
     * @param name
     *        The simple name for this annotation
     *
     * @param value
     *        The boolean value for this annotation
     * @return
     *         The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
     *
     */
    public JAnnotationUse param(String name, boolean value){
         JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
         memberValues.put(name,annotationValue);
         return this;
    }

    /**
     * Adds a member value pair to this annotation
     * @param name
     *        The simple name for this annotation
     *
     * @param value
     *        The int member value for this annotation
     * @return
     *         The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
     *
     */
    public JAnnotationUse param(String name, int value){
         JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
         memberValues.put(name,annotationValue);
         return this;
    }

    /**
     * Adds a member value pair to this annotation
     * @param name
     *        The simple name for this annotation
     *
     * @param value
     *        The String member value for this annotation
     * @return
     *         The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
     *
     */
    public JAnnotationUse param(String name, String value){
        //Escape string values with quotes so that they can
        //be generated accordingly
        JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value));
        memberValues.put(name,annotationValue);
        return this;
    }

    /**
     * Adds a member value pair to this annotation
     * For adding class values as param
     * @see #param(java.lang.String, java.lang.Class)
     * @param name
     *        The simple name for this annotation
     *
     * @param value
     *        The annotation class which is member value for this annotation
     * @return
     *         The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
     *
     */
     public JAnnotationUse annotationParam(String name, Class <? extends Annotation> value){
         JAnnotationUse annotationUse = new JAnnotationUse(owner().ref(value));
         memberValues.put(name,annotationUse);
         return annotationUse;
    }

     /**
     * Adds a member value pair to this annotation
     * @param name
     *        The simple name for this annotation
     *
     * @param value
     *        The enum class which is member value for this annotation
     * @return
     *         The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
     *
     */
     public JAnnotationUse param(String name, final Enum value){
         memberValues.put (name, new JAnnotationValue() {
             public void generate(JFormatter f) {
                 f.t(owner().ref(value.getDeclaringClass())).p('.').p(value.name());
             }
         });
         return this;
    }

    /**
     * Adds a member value pair to this annotation
     * @param name
     *        The simple name for this annotation
     *
     * @param value
     *        The JEnumConstant which is member value for this annotation
     * @return
     *         The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
     *
     */
     public JAnnotationUse param(String name, JEnumConstant value){
         JAnnotationValue annotationValue = new JAnnotationStringValue(value);
         memberValues.put (name, annotationValue);
         return this;
    }

     /**
     * Adds a member value pair to this annotation
     *  This can be used for e.g to specify
      * <pre>
     *        &#64;XmlCollectionItem(type=Integer.class);
      * <pre>
     * For adding a value of Class<? extends Annotation>
     * @link
      * #annotationParam(java.lang.String, java.lang.Class<? extends java.lang.annotation.Annotation>)
     * @param name
     *        The simple name for this annotation param
     *
     * @param value
     *        The class type of the param
     * @return
     *         The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
      *
      *
     *
     */
     public JAnnotationUse param(String name, Class value){
        JAnnotationValue annotationValue = new JAnnotationStringValue(JExpr.lit(value.getName()));
        memberValues.put(name,annotationValue);
        return this;
    }

    /**
     * Adds a member value pair to this annotation based on the
     * type represented by the given JType
     *
     * @param name The simple name for this annotation param
     * @param type the JType representing the actual type
     * @return The JAnnotationUse. More member value pairs can
     *         be added to it using the same or the overloaded methods.
     */
    public JAnnotationUse param(String name, JType type){
        JClass clazz = type.boxify();
        JAnnotationValue annotationValue = new JAnnotationStringValue ( clazz.dotclass() );
        memberValues.put(name,annotationValue);
        return this;
    }

    /**
     * Adds a member value pair which is of type array to this annotation
     * @param name
     *        The simple name for this annotation
     *
     * @return
     *         The JAnnotationArrayMember. For adding array values
     *         @see JAnnotationArrayMember
     *
     */
    public JAnnotationArrayMember paramArray(String name){
        JAnnotationArrayMember arrayMember = new JAnnotationArrayMember(owner());
        memberValues.put(name,arrayMember);
        return arrayMember;
    }


    /**
     * This can be used to add annotations inside annotations
     * for e.g  &#64;XmlCollection(values= &#64;XmlCollectionItem(type=Foo.class))
     * @param className
     *         The classname of the annotation to be included
     * @return
     *         The JAnnotationUse that can be used as a member within this JAnnotationUse
     * @deprecated
     *      use {@link JAnnotationArrayMember#annotate}
     */
    public JAnnotationUse annotate(String className) {
        try {
            JAnnotationUse annotationUse = new JAnnotationUse(owner().ref(className));
            return annotationUse;
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    /**
     * This can be used to add annotations inside annotations
     * for e.g  &#64;XmlCollection(values= &#64;XmlCollectionItem(type=Foo.class))
     * @param clazz
     *         The annotation class to be included
     * @return
     *     The JAnnotationUse that can be used as a member within this JAnnotationUse
     * @deprecated
     *      use {@link JAnnotationArrayMember#annotate}
     */
    public JAnnotationUse annotate(Class <? extends Annotation> clazz) {
         JAnnotationUse annotationUse = new JAnnotationUse(owner().ref(clazz));
         return annotationUse;
    }

    public void generate(JFormatter f) {
        f.p('@').g(clazz);
        boolean first;
        if (memberValues.size() != 0 ) {
            f.p('(');
            first = true;

            for (Iterator iterator = memberValues.entrySet().iterator();iterator.hasNext();) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                String membername = (String)mapEntry.getKey();
                if (!first) {
                    f.p(',');
                }
                f.p(membername).p('=');
                JAnnotationValue memberVal = (JAnnotationValue) mapEntry.getValue();
                f.g(memberVal);
                first = false;
            }
            f.p(')');
        }
    }

}

