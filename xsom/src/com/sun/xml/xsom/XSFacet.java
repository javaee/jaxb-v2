/*
 * @(#)$Id: XSFacet.java,v 1.1 2005-04-14 22:06:20 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

import org.relaxng.datatype.ValidationContext;

/**
 * Facet for a simple type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSFacet extends XSComponent
{
    /** Gets the name of the facet, such as "length". */
    String getName();
    
    /** Gets the value of the facet. */
    String getValue();
    
    /**
     * Gets the context in which the facet value was found.
     * 
     * <p>
     * The primary use of the ValidationContext is to resolve the
     * namespace prefix of the value when it is a QName.
     */
    ValidationContext getContext();
    
    /** Returns true if this facet is "fixed". */
    boolean isFixed();
    
    
    // well-known facet name constants
    final static String FACET_LENGTH            = "length";
    final static String FACET_MINLENGTH         = "minLength";
    final static String FACET_MAXLENGTH         = "maxLength";
    final static String FACET_PATTERN           = "pattern";
    final static String FACET_ENUMERATION       = "enumeration";
    final static String FACET_TOTALDIGITS       = "totalDigits";
    final static String FACET_FRACTIONDIGITS    = "fractionDigits";
    final static String FACET_MININCLUSIVE      = "minInclusive";
    final static String FACET_MAXINCLUSIVE      = "maxInclusive";
    final static String FACET_MINEXCLUSIVE      = "minExclusive";
    final static String FACET_MAXEXCLUSIVE      = "maxExclusive";
    final static String FACET_WHITESPACE        = "whiteSpace";
}
