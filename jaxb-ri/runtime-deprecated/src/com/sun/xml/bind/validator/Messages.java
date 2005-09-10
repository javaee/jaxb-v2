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

package com.sun.xml.bind.validator;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 * 
 * @since JAXB1.0
 */
public class Messages
{
    public static String format( String property ) {
        return format( property, null );
    }
    
    public static String format( String property, Object arg1 ) {
        return format( property, new Object[]{arg1} );
    }
    
    public static String format( String property, Object arg1, Object arg2 ) {
        return format( property, new Object[]{arg1,arg2} );
    }
    
    public static String format( String property, Object arg1, Object arg2, Object arg3 ) {
        return format( property, new Object[]{arg1,arg2,arg3} );
    }
    
    // add more if necessary.
    
    /** Loads a string resource and formats it with specified arguments. */
    public static String format( String property, Object[] args ) {
        String text = ResourceBundle.getBundle(Messages.class.getName()).getString(property);
        return MessageFormat.format(text,args);
    }
    
//
//
// Message resources
//
//
    public static final String INCORRECT_CHILD_FOR_WILDCARD = // 2 args
        "MSVValidator.IncorrectChildForWildcard";

    public static final String DUPLICATE_ID = // 1 arg
        "ValidationContext.DuplicateId";

    public static final String ID_NOT_FOUND = // 0 args
        "ValidationContext.IdNotFound";

    /** @deprecated use MISSING_OBJECT2 */
    public static final String MISSING_OBJECT = // 0 args
        "Validator.MissingObject";

    /**
     * @deprecated
     *  use {@link com.sun.xml.bind.serializer.Util#createMissingObjectError(JAXBObject, String)}
     */
    public static final String MISSING_OBJECT2 = // 1 arg
        "Validator.MissingObject2";
        
    public static final String NOT_VALIDATABLE = // 0 args
        "Validator.NotValidatable";

    public static final String CYCLE_DETECTED = // 0 args
        "ValidationContext.CycleDetected";
        
    public static final String MUST_NOT_BE_NULL = // 1 arg
        "Shared.MustNotBeNull";
}
