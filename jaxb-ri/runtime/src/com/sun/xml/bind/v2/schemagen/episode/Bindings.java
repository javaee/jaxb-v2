package com.sun.xml.bind.v2.schemagen.episode;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.annotation.XmlAttribute;

/**
 * @author Kohsuke Kawaguchi
 */
@XmlElement("bindings")
public interface Bindings extends TypedXmlWriter {
    /**
     * Nested bindings.
     */
    @XmlElement
    Bindings bindings();

    /**
     * Nested class customization.
     */
    @XmlElement("class")
    Klass klass();

    @XmlElement
    SchemaBindings schemaBindings();

    @XmlAttribute
    void scd(String scd);

    @XmlAttribute
    void version(String v);
}
