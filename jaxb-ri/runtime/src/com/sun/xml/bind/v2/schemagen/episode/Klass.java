package com.sun.xml.bind.v2.schemagen.episode;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Klass extends TypedXmlWriter {
    /**
     * FQCN.
     */
    @XmlAttribute
    void ref(String className);
}
