package com.sun.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Used on a field/property of the type
 * {@link Map}&lt;{@link QName},{@link Object}> or
 * {@link Map}&lt;{@link QName},{@link String}> to catch
 * the rest of the attributes that aren't caught by {@link XmlAttribute}s.
 *
 * TO BE MOVED TO THE API MODULE.
 *
 * <p>
 * If the value type of the map is {@link String}, the map will hold
 * attribute values as is keyed by their attribute names. If the value type
 * of the map is {@link Object}, the map will hold the strongly-typed values
 * according to {@link XmlAttributeMapping} annotations.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlAnyAttribute {
}
