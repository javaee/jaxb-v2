package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Kohsuke Kawaguchi
 */
enum Messages {
    UNRESOLVED_IDREF, // 1 arg
    UNEXPECTED_ELEMENT, // 3 args
    UNEXPECTED_TEXT, // 1 arg
    NOT_A_QNAME,    // 1 arg
    UNRECOGNIZED_TYPE_NAME, // 1 arg
    UNRECOGNIZED_TYPE_NAME_MAYBE, // 2 args
    UNABLE_TO_CREATE_MAP, // 1 arg
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
