/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.xmlschema;

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


    static final String WARN_NO_GLOBAL_ELEMENT =
        "BGMBuilder.NoGlobalElement";

    static final String ERR_MULTIPLE_SCHEMA_BINDINGS =
        "BGMBuilder.MultipleSchemaBindings"; // arg:1

    static final String ERR_MULTIPLE_SCHEMA_BINDINGS_LOCATION =
        "BGMBuilder.MultipleSchemaBindings.Location"; // arg:0

    static final String JAVADOC_HEADING = // 1 arg
        "ClassSelector.JavadocHeading";

    static final String ERR_RESERVED_CLASS_NAME = // 1 arg
        "ClassSelector.ReservedClassName";

    static final String ERR_CLASS_NAME_IS_REQUIRED =
        "ClassSelector.ClassNameIsRequired";    // arg:0

    static final String ERR_INCORRECT_CLASS_NAME =
        "ClassSelector.IncorrectClassName";     // arg:1

    static final String ERR_INCORRECT_PACKAGE_NAME =
        "ClassSelector.IncorrectPackageName";   // arg:2

    static final String ERR_CANNOT_BE_TYPE_SAFE_ENUM =
        "ConversionFinder.CannotBeTypeSafeEnum";            // arg:0

    static final String ERR_CANNOT_BE_TYPE_SAFE_ENUM_LOCATION =
        "ConversionFinder.CannotBeTypeSafeEnum.Location";    // arg:0

    static final String ERR_NO_ENUM_NAME_AVAILABLE =
        "ConversionFinder.NoEnumNameAvailable"; // arg:0

    static final String ERR_ILLEGAL_EXPECTED_MIME_TYPE =
        "ERR_ILLEGAL_EXPECTED_MIME_TYPE"; // args:2

    static final String ERR_DATATYPE_ERROR =
        "DatatypeBuilder.DatatypeError"; // arg:1

    static final String ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP =
        "DefaultParticleBinder.UnableToGenerateNameFromModelGroup"; // arg:0

    static final String ERR_INCORRECT_FIXED_VALUE =
        "FieldBuilder.IncorrectFixedValue"; // arg:1

    static final String ERR_INCORRECT_DEFAULT_VALUE =
        "FieldBuilder.IncorrectDefaultValue"; // arg:1

    static final String ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE =
        "FieldBuilder.ConflictBetweenUserTypeAndActualType.AttUse"; // arg:2

    static final String ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE_SOURCE =
        "FieldBuilder.ConflictBetweenUserTypeAndActualType.AttUse.Source"; // arg:0

    static final String ERR_UNNESTED_JAVATYPE_CUSTOMIZATION_ON_SIMPLETYPE =
        "SimpleTypeBuilder.UnnestedJavaTypeCustomization"; // arg:0

    static final String JAVADOC_NIL_PROPERTY =
        "FieldBuilder.Javadoc.NilProperty"; // arg:1

    static final String JAVADOC_LINE_UNKNOWN = // 0 args
        "ClassSelector.JavadocLineUnknown";
    
    static final String JAVADOC_VALUEOBJECT_PROPERTY =
        "FieldBuilder.Javadoc.ValueObject"; // args:2

    static final String MSG_COLLISION_INFO =
        "CollisionInfo.CollisionInfo"; // args:3

    static final String MSG_UNKNOWN_FILE =
        "CollisionInfo.UnknownFile"; // arg:1

    static final String MSG_LINE_X_OF_Y =
        "CollisionInfo.LineXOfY"; // args:2

    static final String MSG_FALLBACK_JAVADOC =
        "DefaultParticleBinder.FallbackJavadoc"; // arg:1




}
