package com.sun.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Used to mark an element property with a list simple type.
 *
 * <p>
 * The final annotation is still to be determined.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME) @Target({FIELD,METHOD,PARAMETER})
public @interface XmlList {
}
