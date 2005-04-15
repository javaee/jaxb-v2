package com.sun.tools.xjc.model;

import java.util.List;

import javax.xml.namespace.QName;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.impl.ArrayInfoImpl;

/**
 * TODO: Because s2j doesn't generate the array binding, this is unnecessary.
 * @author Kohsuke Kawaguchi
 */
public final class CArrayInfo extends AbstractCTypeInfoImpl implements ArrayInfo<NType,NClass>, CNonElement, NType {

    private final CNonElement itemType;

    private final QName typeName;

    public CArrayInfo(CNonElement itemType, List<CPluginCustomization> customizations) {
        super(customizations);
        this.itemType = itemType;
        assert itemType.getTypeName()!=null;
        this.typeName = ArrayInfoImpl.calcArrayTypeName(itemType.getTypeName());
    }

    public CNonElement getItemType() {
        return itemType;
    }

    public QName getTypeName() {
        return typeName;
    }

    public JType toType(Outline o, Aspect aspect) {
        return itemType.toType(o,aspect).array();
    }

    public NType getType() {
        return this;
    }

    public boolean isBoxedType() {
        return false;
    }

    public String fullName() {
        return itemType.getType().fullName()+"[]";
    }
}
