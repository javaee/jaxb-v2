package com.sun.tools.xjc.reader.xmlschema.ct;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources
 */
enum Messages {
    ERR_NO_FURTHER_EXTENSION;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
