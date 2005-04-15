package com.sun.istack;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    // Accessor
    VERSION, // no arg
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String get() {
        return rb.getString(name());
    }

    public String format( Object... args ) {
        return MessageFormat.format( get(), args );
    }
}
