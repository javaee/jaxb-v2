package com.sun.tools.xjc.api.impl.j2s;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Kohsuke Kawaguchi
 */
enum Messages {
    ;


    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
