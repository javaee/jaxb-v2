package com.sun.xml.bind.v2.model.impl;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    // ClassInfoImpl
    ID_MUST_BE_STRING, // 1 arg

    MUTUALLY_EXCLUSIVE_ANNOTATIONS,  // 2 args
    NO_DEFAULT_CONSTRUCTOR, // 1 arg
    ANNOTATION_ON_WRONG_METHOD, // 0 args
    GETTER_SETTER_INCOMPATIBLE_TYPE, // 2 args

    XML_ELEMENT_MAPPING_ON_NON_IXMLELEMENT_METHOD, // 1 arg
    SCOPE_IS_NOT_COMPLEXTYPE, // 1 arg
    CONFLICTING_XML_ELEMENT_MAPPING,    // 2 args

    REFERENCE_TO_NON_ELEMENT, // 1 arg

    NON_EXISTENT_ELEMENT_MAPPING, // 2 args

    TWO_ATTRIBUTE_WILDCARDS, // 1 arg
    SUPER_CLASS_HAS_WILDCARD, // 0 args
    INVALID_ATTRIBUTE_WILDCARD_TYPE, // 1 arg
    PROPERTY_MISSING_FROM_ORDER, // 1 arg
    PROPERTY_ORDER_CONTAINS_UNUSED_ENTRY, // 1 arg

    INVALID_XML_ENUM_VALUE, // 2 arg
    FAILED_TO_INITIALE_DATATYPE_FACTORY, // 0 args
    NO_IMAGE_WRITER, // 1 arg

    ILLEGAL_TYPE_FOR_ATTRIBUTE, // 1 arg
    ILLEGAL_TYPE_FOR_VALUE, // 1 arg
    ILLEGAL_MIME_TYPE, // 2 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
