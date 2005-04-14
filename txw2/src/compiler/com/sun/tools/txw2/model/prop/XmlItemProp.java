package com.sun.tools.txw2.model.prop;

import com.sun.codemodel.JType;

import javax.xml.namespace.QName;

/**
 * Common implementation between elements and attributes.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class XmlItemProp extends Prop {
    private final QName name;
    private final JType type;

    public XmlItemProp(QName name, JType valueType) {
        this.name = name;
        this.type = valueType;
    }

    public final boolean equals(Object o) {
        if (this.getClass()!=o.getClass()) return false;

        XmlItemProp that = (XmlItemProp)o;

        return this.name.equals(that.name)
            && this.type.equals(that.type);
    }

    public final int hashCode() {
        return name.hashCode()*29 + type.hashCode();
    }
}
