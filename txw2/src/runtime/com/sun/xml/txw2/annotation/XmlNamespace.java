package com.sun.xml.txw2.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.PACKAGE;
import com.sun.xml.txw2.TypedXmlWriter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declares the namespace URI of the {@link TypedXmlWriter}s
 * in a package.
 *
 * <p>
 * This annotation is placed on a package. When specified,
 * it sets the default value of the namespace URI for
 * all the elements ({@link XmlElement}s) in the given package.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({PACKAGE})
public @interface XmlNamespace {
    /**
     * The namespace URI.
     */
    String value();
}
