package com.sun.istack;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A debug annotation that forces the istack annotation processor to run.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(SOURCE)
@Target({TYPE,ANNOTATION_TYPE})
public @interface Run {
}
