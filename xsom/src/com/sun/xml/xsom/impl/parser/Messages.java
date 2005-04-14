/*
 * @(#)$Id: Messages.java,v 1.1 2005-04-14 22:06:29 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.parser;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
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
        String text = ResourceBundle.getBundle(
            Messages.class.getName()).getString(property);
        return MessageFormat.format(text,args);
    }
    
//
//
// Message resources
//
//
    public static final String ERR_UNDEFINED_SIMPLETYPE =
        "UndefinedSimpleType"; // arg:1
    public static final String ERR_UNDEFINED_COMPLEXTYPE =
        "UndefinedCompplexType"; // arg:1
    public static final String ERR_UNDEFINED_TYPE =
        "UndefinedType"; // arg:1
    public static final String ERR_UNDEFINED_ELEMENT =
        "UndefinedElement"; // arg:1
    public static final String ERR_UNDEFINED_MODELGROUP =
        "UndefinedModelGroup"; // arg:1
    public static final String ERR_UNDEFINED_ATTRIBUTE =
        "UndefinedAttribute"; // arg:1
    public static final String ERR_UNDEFINED_ATTRIBUTEGROUP =
        "UndefinedAttributeGroup"; // arg:1
    public static final String ERR_UNDEFINED_IDENTITY_CONSTRAINT =
        "UndefinedIdentityConstraint"; // arg:1
    public static final String ERR_UNDEFINED_PREFIX =
        "UndefinedPrefix"; // arg:1

    public static final String ERR_DOUBLE_DEFINITION =
        "DoubleDefinition"; // arg:1
    public static final String ERR_DOUBLE_DEFINITION_ORIGINAL =
        "DoubleDefinition.Original"; // arg:0
    
    public static final String ERR_MISSING_SCHEMALOCATION =
        "MissingSchemaLocation"; // arg:0
        
    public static final String ERR_ENTITY_RESOLUTION_FAILURE =
        "EntityResolutionFailure"; // arg:2
}
