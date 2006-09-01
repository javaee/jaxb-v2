package com.sun.tools.xjc.addon.episode;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

/**
 * @author Kohsuke Kawaguchi
 */
interface Klass extends TypedXmlWriter {
    /**
     * FQCN.
     */
    @XmlAttribute
    void ref(String className);
}
