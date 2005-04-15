package com.sun.tools.xjc.api;

import java.util.List;

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeAndAnnotation {
    /**
     * Returns the fully-qualified name of the java type.
     *
     * @return
     *      never be null.
     */
    String getTypeClass();

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
