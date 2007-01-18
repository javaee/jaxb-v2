/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.xjc.reader;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
public enum Messages {
    DUPLICATE_PROPERTY, // 1 arg

    ERR_UNDECLARED_PREFIX,
    ERR_UNEXPECTED_EXTENSION_BINDING_PREFIXES,
    ERR_UNSUPPORTED_EXTENSION,
    ERR_SUPPORTED_EXTENSION_IGNORED,
    ERR_RELEVANT_LOCATION,
    ERR_CLASS_NOT_FOUND,
    PROPERTY_CLASS_IS_RESERVED,
    ERR_VENDOR_EXTENSION_DISALLOWED_IN_STRICT_MODE,
    ERR_ILLEGAL_CUSTOMIZATION_TAGNAME, // 1 arg
    ERR_PLUGIN_NOT_ENABLED, // 2 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getPackage().getName() +".MessageBundle");

    public String toString() {
        return format();
    }

    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
