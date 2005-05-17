package com.sun.xml.bind.v2.runtime.property;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    NOT_A_QNAME,    // 1 arg
    UNRECOGNIZED_TYPE_NAME, // 1 arg
    UNSUBSTITUTABLE_TYPE, // 3 args
    UNEXPECTED_JAVA_TYPE, // 2 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
