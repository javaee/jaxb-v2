package com.sun.tools.xjc.api;

import com.sun.codemodel.JAnnotatable;
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
     * Annotates the given program element by additional JAXB annotations that need to be there
     * at the point of reference.
     */
    void annotate( JAnnotatable programElement );

    /**
     * Two {@link TypeAndAnnotation} are equal if they
     * has the same type and annotations.
     */
    boolean equals(Object o);
}
