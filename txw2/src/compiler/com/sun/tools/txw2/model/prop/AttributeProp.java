package com.sun.tools.txw2.model.prop;

import com.sun.codemodel.JType;

import javax.xml.namespace.QName;

/**
 * @author Kohsuke Kawaguchi
 */
public final class AttributeProp extends XmlItemProp {
    public AttributeProp(QName name, JType valueType) {
        super(name, valueType);
    }
}
