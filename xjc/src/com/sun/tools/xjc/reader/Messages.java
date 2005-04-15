/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
enum Messages {
    DUPLICATE_PROPERTY, // 1 args

    ERR_UNDECLARED_PREFIX,
    ERR_UNEXPECTED_EXTENSION_BINDING_PREFIXES,
    ERR_UNSUPPORTED_EXTENSION,
    ERR_SUPPORTED_EXTENSION_IGNORED,
    ERR_RELEVANT_LOCATION,
    ERR_CLASS_NOT_FOUND,
    PROPERTY_CLASS_IS_RESERVED,
    ERR_VENDOR_EXTENSION_DISALLOWED_IN_STRICT_MODE,

    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
