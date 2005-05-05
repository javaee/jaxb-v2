package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;

import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi
 */
public final class CValuePropertyInfo extends CSingleTypePropertyInfo implements ValuePropertyInfo<NType,NClass> {
    public CValuePropertyInfo(String name, CCustomizations customizations, Locator locator, TypeUse type) {
        super(name, type, customizations, locator);
    }

    public final PropertyKind kind() {
        return  PropertyKind.VALUE;
    }
}
