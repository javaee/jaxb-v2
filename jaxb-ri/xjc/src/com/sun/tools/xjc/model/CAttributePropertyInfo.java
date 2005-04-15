package com.sun.tools.xjc.model;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;

import org.xml.sax.Locator;

/**
 * {@link AttributePropertyInfo} for the compiler.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CAttributePropertyInfo extends CPropertyInfo implements AttributePropertyInfo<NType,NClass> {

    private final QName attName;
    private final TypeUse type;
    private final boolean isRequired;

    public CAttributePropertyInfo(String name, List<CPluginCustomization> customizations,
                                  Locator locator, QName attName, TypeUse type, boolean required ) {
        super(name, type.isCollection(), type.idUse(), customizations, locator);
        isRequired = required;
        this.type = type;
        this.attName = attName;
    }

    public boolean isRequired() {
        return isRequired;
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

    public QName getXmlName() {
        return attName;
    }

    /**
     * An optional attribute can never be unboxable,
     * for we need null to represent the absence.
     */
    public boolean isUnboxable() {
        if(!isRequired) return false;
        return super.isUnboxable();
    }

    public final PropertyKind kind() {
        return  PropertyKind.ATTRIBUTE;
    }
}
