package com.sun.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.awt.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import javax.xml.transform.Source;

/**
 * Associates the MIME type that controls the XML representation of the property.
 *
 * <p>
 * This annotation is used in conjunction with datatypes such as
 * {@link Image} or {@link Source} that are bound to base64-encoded binary in XML.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlMimeType {
    /**
     * The textual representation of the MIME type,
     * such as "image/jpeg" "image/*", "text/xml; charset=iso-8859-1" and so on.
     */
    String value();
}
