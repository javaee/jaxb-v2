package com.sun.tools.txw2.model.prop;

import com.sun.codemodel.JType;

import javax.xml.namespace.QName;

/**
 * Property generated from elements.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ElementProp extends XmlItemProp {
    public ElementProp(QName name, JType valueType) {
        super(name, valueType);
    }
}
