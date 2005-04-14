package com.sun.xml.txw2.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static java.lang.annotation.ElementType.METHOD;

import com.sun.xml.txw2.DatatypeWriter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that the invocation of the method will produce a text
 *
 * <p>
 * The method signature has to match the form <tt>R foo(DT1,DT2,..)</tt>
 *
 * <p>
 * R is either <tt>void</tt> or the type to which the interface that declares
 * this method is assignable. In the case of the latter, the method will return
 * <tt>this</tt> object, allowing you to chain the multiple method
 * invocations like {@link StringBuffer}.
 *
 * <p>
 * DTi must be datatype objects.
 *
 * <p>
 * When this method is called, whitespace-separated text data
 * is added from each of the datatype objects.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface XmlValue {
}
