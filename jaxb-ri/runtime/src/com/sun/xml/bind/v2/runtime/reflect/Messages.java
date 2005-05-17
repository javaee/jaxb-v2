package com.sun.xml.bind.v2.runtime.reflect;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    // Accessor
    UNABLE_TO_ACCESS_NON_PUBLIC_FIELD,  // 2 args
    UNABLE_TO_FIND_CONSTRUCTOR, // 2 args
    UNASSIGNABLE_TYPE, // 2 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
