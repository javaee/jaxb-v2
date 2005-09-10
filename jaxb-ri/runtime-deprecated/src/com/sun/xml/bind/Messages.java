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

package com.sun.xml.bind;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 * 
 * @since 1.0
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
    public static final String CI_NOT_NULL= // 0 args
        "DefaultJAXBContextImpl.CINotNull";
       
    public static final String CI_CI_NOT_NULL = // 0 args
        "DefaultJAXBContextImpl.CICINotNull";
        
    public static final String NO_BGM = // 1 arg
        "GrammarInfo.NoBGM";
        
    public static final String UNABLE_TO_READ_BGM = // 0 args
        "GrammarInfo.UnableToReadBGM";

    public static final String COLLISION_DETECTED = // 2 args
        "GrammarInfoFacade.CollisionDetected";

    public static final String INCOMPATIBLE_VERSION = // 3 args
        "GrammarInfoFacade.IncompatibleVersion";

    public static final String MISSING_INTERFACE = // 1 arg
        "ImplementationRegistry.MissingInterface";

    public static final String BUILD_ID = // 0 args
        "DefaultJAXBContextImpl.buildID";
    
    public static final String INCORRECT_VERSION =
        "ContextFactory.IncorrectVersion";

    public static final String ERR_TYPE_MISMATCH = // arg:3 since JAXB 1.0.3
        "Util.TypeMismatch";
    
/*        
    static final String = // arg
        "";
    static final String = // arg
        "";
    static final String = // arg
        "";
    static final String = // arg
        "";
  */      
}
