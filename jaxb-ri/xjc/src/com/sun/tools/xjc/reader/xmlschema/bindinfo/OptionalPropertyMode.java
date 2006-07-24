package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Represents three constants of globalBindings/@optionalProperty.
 *
 * @author Kohsuke Kawaguchi
 */
public enum OptionalPropertyMode {
    @XmlEnumValue("primitive")
    PRIMITIVE,
    @XmlEnumValue("wrapper")
    WRAPPER,
    @XmlEnumValue("isSet")
    ISSET
}
