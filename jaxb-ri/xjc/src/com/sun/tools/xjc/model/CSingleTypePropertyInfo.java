package com.sun.tools.xjc.model;

import java.util.Collections;
import java.util.List;

import javax.activation.MimeType;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * {@link CPropertyInfo} backed by a single {@link TypeUse}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class CSingleTypePropertyInfo extends CPropertyInfo {
    protected final TypeUse type;

    /**
     *
     * @param typeName
     *      XML Schema type name of this property's single value. Optional
     *      for other schema languages. This is used to determine if we should
     *      generate {@link @XmlSchemaType} annotation to improve the roundtrip.
     */
    protected CSingleTypePropertyInfo(String name, TypeUse type, QName typeName, XSComponent source, CCustomizations customizations, Locator locator) {
        super(name, type.isCollection(), source, customizations, locator);
        this.type = type;

        if(needsExplicitTypeName(type,typeName))
            schemaType = typeName;
    }

    private static boolean needsExplicitTypeName(TypeUse type, QName typeName) {
        if(typeName==null)
            // this is anonymous type. can't have @XmlSchemaType
            return false;

        if(!typeName.getNamespaceURI().equals(WellKnownNamespace.XML_SCHEMA))
            // if we put application-defined type name, it will be undefined
            // by the time we generate a schema.
            return false;

        if(type.isCollection())
            // there's no built-in binding for a list simple type,
            // so any collection type always need @XmlSchemaType
            return true;

        QName itemType = type.getInfo().getTypeName();
        if(itemType==null)
            // this is somewhat strange case, as it means the bound type is anonymous
            // but it's eventually derived by a named type and used.
            // but we can certainly use typeName as @XmlSchemaType value here
            return true;

        // if it's the default type name for this item, then no need
        return !itemType.equals(typeName);
    }

    public final ID id() {
        return type.idUse();
    }

    public final MimeType getExpectedMimeType() {
        return type.getExpectedMimeType();
    }

    public final List<? extends CTypeInfo> ref() {
        return Collections.singletonList(getTarget());
    }

    public final CNonElement getTarget() {
        CNonElement r = type.getInfo();
        assert r!=null;
        return r;
    }

    public final CAdapter getAdapter() {
        return type.getAdapterUse();
    }

    public final CSingleTypePropertyInfo getSource() {
        return this;
    }
}
