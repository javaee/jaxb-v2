/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.generator.bean;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
enum Messages {
    // AnnotationParser
    METHOD_COLLISION, // 3 args
    ERR_UNUSABLE_NAME, // 2 args
    ERR_NAME_COLLISION, // 1 arg
    ILLEGAL_CONSTRUCTOR_PARAM, // 1 arg
    OBJECT_FACTORY_CONFLICT,    // 1 arg
    OBJECT_FACTORY_CONFLICT_RELATED,
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".MessageBundle");

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
