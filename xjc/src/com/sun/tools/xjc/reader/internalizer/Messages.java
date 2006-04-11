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

package com.sun.tools.xjc.reader.internalizer;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
class Messages
{
    /** Loads a string resource and formats it with specified arguments. */
    static String format( String property, Object... args ) {
        String text = ResourceBundle.getBundle(Messages.class.getPackage().getName() +".MessageBundle").getString(property);
        return MessageFormat.format(text,args);
    }
    
    static final String ERR_INCORRECT_SCHEMA_REFERENCE = // args:2
        "Internalizer.IncorrectSchemaReference";
    static final String ERR_XPATH_EVAL = // arg:1
        "Internalizer.XPathEvaluationError";
    static final String NO_XPATH_EVAL_TO_NO_TARGET = // arg:1
        "Internalizer.XPathEvaluatesToNoTarget";
    static final String NO_XPATH_EVAL_TOO_MANY_TARGETS = // arg:2
        "Internalizer.XPathEvaulatesToTooManyTargets";
    static final String NO_XPATH_EVAL_TO_NON_ELEMENT = // arg:1
        "Internalizer.XPathEvaluatesToNonElement";
    static final String XPATH_EVAL_TO_NON_SCHEMA_ELEMENT = // arg:2
        "Internalizer.XPathEvaluatesToNonSchemaElement";
    static final String CONTEXT_NODE_IS_NOT_ELEMENT = // arg:0
        "Internalizer.ContextNodeIsNotElement";
    static final String NO_CONTEXT_NODE_SPECIFIED = // arg:0
            "Internalizer.NoContextNodeSpecified";
    static final String ERR_INCORRECT_VERSION = // arg:0
        "Internalizer.IncorrectVersion";
    static final String ERR_VERSION_NOT_FOUND = // arg:0
        "Internalizer.VersionNotPresent";
    static final String TWO_VERSION_ATTRIBUTES = // arg:0
        "Internalizer.TwoVersionAttributes";
    static final String ORPHANED_CUSTOMIZATION = // arg:1
        "Internalizer.OrphanedCustomization";
    static final String ERR_UNABLE_TO_PARSE = // arg:2
        "AbstractReferenceFinderImpl.UnableToParse";
    static final String ERR_GENERAL_SCHEMA_CORRECTNESS_ERROR = // arg:1
        "ERR_GENERAL_SCHEMA_CORRECTNESS_ERROR";
}
