package com.sun.tools.xjc.model;

import java.util.Collections;
import java.util.List;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;

import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi
 */
public final class CValuePropertyInfo extends CPropertyInfo implements ValuePropertyInfo<NType,NClass> {
    private final TypeUse type;

    public CValuePropertyInfo(String name, List<CPluginCustomization> customizations, Locator locator, TypeUse type) {
        super(name, type.isCollection(), type.idUse(), customizations, locator);
        this.type = type;
    }

    public List<? extends CTypeInfo> ref() {
        return Collections.singletonList(getType());
    }

    public CNonElement getType() {
        CNonElement r = (CNonElement)type.getInfo();
        assert r!=null;
        return r;
    }

    public CAdapter getAdapter() {
        return type.getAdapterUse();
    }

    public final PropertyKind kind() {
        return  PropertyKind.VALUE;
    }
}
