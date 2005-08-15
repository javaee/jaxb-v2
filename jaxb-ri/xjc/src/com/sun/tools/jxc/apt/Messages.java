package com.sun.tools.jxc.apt;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources.
 *
 * @author Kohsuke Kawaguchi
 */
enum Messages {
    // Accessor
    NON_EXISTENT_FILE, // 1 arg
    NO_FILE_SPECIFIED, // 0 args
    UNRECOGNIZED_PARAMETER, //1 arg
    USAGE, //0 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getPackage().getName() +".MessageBundle");

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
