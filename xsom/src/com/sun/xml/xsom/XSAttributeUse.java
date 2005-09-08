/*
 * @(#)$Id: XSAttributeUse.java,v 1.2 2005-09-08 22:49:31 kohsuke Exp $
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
 * Attribute use.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSAttributeUse extends XSComponent
{
    boolean isRequired();
    XSAttributeDecl getDecl();

    /**
     * Gets the default value of this attribute use, if one is specified.
     * 
     * Note that if a default value is specified in the attribute
     * declaration, this method returns that value.
     */
    XmlString getDefaultValue();

    /**
     * Gets the fixed value of this attribute use, if one is specified.
     * 
     * Note that if a fixed value is specified in the attribute
     * declaration, this method returns that value.
     */
    XmlString getFixedValue();
}
