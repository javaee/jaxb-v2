package com.sun.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Marks a field/property that in XML it is attached as a
 * WS-I BP 1.1 SOAP attachment.
 *
 * A field/property must always map to the {@link DataHandler} class.
 *
 * <h2>Usage</h2>
 * <pre>
 * &#64;{@link XmlRootElement}
 * class Foo {
 *   &#64;{@link XmlSoapAttachment}
 *   &#64;{@link XmlAttribute}
 *   {@link DataHandler} data;
 *
 *   &#64;{@link XmlSoapAttachment}
 *   &#64;{@link XmlElement}
 *   {@link DataHandler} body;
 * }
 * </pre>
 * The above code would map to the following XML:
 * <pre><xmp>
 * <xs:element name="foo" xmlns:ref="http://ws-i.org/profiles/basic/1.1/xsd">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="body" type="ref:swaRef" minOccurs="0" />
 *     </xs:sequence>
 *     <xs:attribute name="data" type="ref:swaRef" use="optional" />
 *   </xs:complexType>
 * </xs:element>
 * </xmp></pre>
 *
 * <p>
 * <b>TO BE MOVED TO THE API MODULE.</b>
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({FIELD,METHOD})
public @interface XmlSoapAttachment {
}
