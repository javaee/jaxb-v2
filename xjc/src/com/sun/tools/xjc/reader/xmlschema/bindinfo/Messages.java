/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
class Messages
{
    /** Loads a string resource and formats it with specified arguments. */
    static String format( String property, Object... args ) {
        String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
        return MessageFormat.format(text,args);
    }
    
    static final String ERR_UNDEFINED_SIMPLE_TYPE = // arg:1
        "UndefinedSimpleType";
}
