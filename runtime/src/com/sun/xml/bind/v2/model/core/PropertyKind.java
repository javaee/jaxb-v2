package com.sun.xml.bind.v2.model.core;

import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;

import com.sun.xml.bind.v2.runtime.property.PropertyFactory;


/**
 * An Enum that indicates if the property is
 * Element, ElementRef, Value, or Attribute.
 *
 * <p>
 * Corresponds to the four different kind of {@link PropertyInfo}.
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
public enum PropertyKind {
    VALUE(true,false,Integer.MAX_VALUE),
    ATTRIBUTE(false,false,Integer.MAX_VALUE),
    ELEMENT(true,true,0),
    REFERENCE(false,true,1),
    MAP(false,true,2),
    ;

    /**
     * This kind of property can have {@link XmlMimeType} annotation with it.
     */
    public final boolean canHaveXmlMimeType;

    /**
     * This kind of properties need to show up in {@link XmlType#propOrder()}.
     */
    public final boolean isOrdered;

    /**
     * {@link PropertyFactory} benefits from having index numbers assigned to
     * {@link #ELEMENT}, {@link REFERENCE}, and {@link MAP} in this order.
     */
    public final int propertyIndex;

    PropertyKind(boolean canHaveExpectedContentType, boolean isOrdered, int propertyIndex) {
        this.canHaveXmlMimeType = canHaveExpectedContentType;
        this.isOrdered = isOrdered;
        this.propertyIndex = propertyIndex;
    }
}
