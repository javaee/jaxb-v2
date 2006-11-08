package com.sun.xml.bind.v2.schemagen;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * Message resources
 */
enum Messages {
    ANONYMOUS_TYPE_CYCLE // 1 arg
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
