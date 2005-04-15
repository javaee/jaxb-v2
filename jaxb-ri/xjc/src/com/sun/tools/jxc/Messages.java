package com.sun.tools.jxc;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Message resources.
 *
 * @author Kohsuke Kawaguchi
 */
enum Messages {
    // Accessor
    UNEXPECTED_NGCC_TOKEN, // 3 args
    BASEDIR_DOESNT_EXIST, // 1 arg
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
