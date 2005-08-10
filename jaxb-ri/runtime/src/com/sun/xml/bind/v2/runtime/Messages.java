package com.sun.xml.bind.v2.runtime;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    ILLEGAL_PARAMETER, // 2 args
    UNABLE_TO_FIND_CONVERSION_METHOD, // 3 args
    MISSING_ID, // 1 arg
    NOT_IMPLEMENTED_IN_2_0,
    UNRECOGNIZED_ELEMENT_NAME,
    TYPE_MISMATCH, // 3 args
    MISSING_OBJECT, // 1 arg
    NOT_IDENTIFIABLE, // 0 args
    DANGLING_IDREF, // 1 arg
    NULL_OUTPUT_RESOLVER, // 0 args
    UNABLE_TO_MARSHAL_NON_ELEMENT, // 1 arg
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
