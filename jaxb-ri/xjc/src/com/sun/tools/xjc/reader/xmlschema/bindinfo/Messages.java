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

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
enum Messages
{
    ERR_CANNOT_BE_BOUND_TO_SIMPLETYPE,
    ERR_UNDEFINED_SIMPLE_TYPE,
    ERR_ILLEGAL_FIXEDATTR
    ;

    /** Loads a string resource and formats it with specified arguments. */
    String format( Object... args ) {
        String text = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".MessageBundle").getString(name());
        return MessageFormat.format(text,args);
    }
}
