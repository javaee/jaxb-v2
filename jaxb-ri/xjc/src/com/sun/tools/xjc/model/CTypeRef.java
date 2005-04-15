package com.sun.tools.xjc.model;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.runtime.Util;

/**
 * @author Kohsuke Kawaguchi
 */
public final class CTypeRef implements TypeRef<NType,NClass> {
    /**
     * In-memory type.
     *
     * This is the type used when 
     */
    @XmlJavaTypeAdapter(Util.ToStringAdapter.class)
    private final CNonElement type;

    private final QName elementName;

    private final boolean nillable;
    private String defaultValue;

    public CTypeRef(CNonElement type, QName elementName, boolean nillable, String defaultValue) {
        assert type!=null;
        assert elementName!=null;

        this.type = type;
        this.elementName = elementName;
        this.nillable = nillable;
        this.defaultValue = defaultValue;
    }

    public CNonElement getType() {
        return type;
    }

    public QName getTagName() {
        return elementName;
    }

    public boolean isNillable() {
        return nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isLeaf() {
        // TODO: implement this method later
        throw new UnsupportedOperationException();
    }
}
