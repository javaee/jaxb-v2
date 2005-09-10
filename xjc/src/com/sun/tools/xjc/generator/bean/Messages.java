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
