package com.sun.tools.xjc.api;

import java.util.List;

import com.sun.codemodel.JType;

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeAndAnnotation {
    /**
     * Returns the Java type.
     *
     * <p>
     * {@link JType} is a representation of a Java type in a codeModel.
     * If you just need the fully-qualified class name, call {@link JType#fullName()}.
     * 
     * @return
     *      never be null.
     */
    JType getTypeClass();

    /**
     * Returns the additional annotations needed to be with the type.
     *
     * <p>
     * Each string in the collection looks like an annotation,
     * such as <tt>@XmlJavaTypeAdapter(FooBar.class)</tt>.
     *
     * @return
     *      can be empty but never be null.
     */
    List<String> getAnnotations();
}
