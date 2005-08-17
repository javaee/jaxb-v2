package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * {@link ValuePropertyInfo} implementation for XJC.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class CValuePropertyInfo extends CSingleTypePropertyInfo implements ValuePropertyInfo<NType,NClass> {
    public CValuePropertyInfo(String name, XSComponent source, CCustomizations customizations, Locator locator, TypeUse type) {
        super(name, type, source, customizations, locator);
    }

    public final PropertyKind kind() {
        return  PropertyKind.VALUE;
    }

    public <V> V accept(CPropertyVisitor<V> visitor) {
        return visitor.onValue(this);
    }
}
