package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Kohsuke Kawaguchi
 */
enum Messages {
    UNRESOLVED_IDREF, // 1 arg
    UNEXPECTED_ROOT_ELEMENT, // 3 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
