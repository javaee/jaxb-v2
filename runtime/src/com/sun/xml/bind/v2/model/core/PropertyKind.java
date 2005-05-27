package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.XmlMimeType;


/**
 * An Enum that indicates if the property is
 * Element, ElementRef, Value, or Attribute.
 *
 * <p>
 * Corresponds to the four different kind of {@link PropertyInfo}.
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
public enum PropertyKind {
    VALUE(true),
    ATTRIBUTE(false),
    ELEMENT(true),
    REFERENCE(false)

    ;

    /**
     * This kind of property can have {@link XmlMimeType} annotation with it.
     */
    public final boolean canHaveXmlMimeType;

    PropertyKind(boolean canHaveExpectedContentType) {
        this.canHaveXmlMimeType = canHaveExpectedContentType;
    }
}
