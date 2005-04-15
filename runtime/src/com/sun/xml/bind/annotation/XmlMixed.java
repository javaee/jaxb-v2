package com.sun.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.xml.bind.annotation.XmlElementRef;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * TO BE MOVED TO THE API MODULE.
 *
 * Used with {@link XmlElementRef} to mark mixed contents.
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlMixed {
}
