package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Kohsuke Kawaguchi
 */
public enum LocalScoping {
    @XmlEnumValue("nested")
    NESTED,
    @XmlEnumValue("toplevel")
    TOPLEVEL
}
