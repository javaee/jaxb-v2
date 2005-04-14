/*
 * @(#)$Id: XSAttributeDecl.java,v 1.1 2005-04-14 22:06:18 kohsuke Exp $
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
 * Attribute declaration.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSAttributeDecl extends XSDeclaration
{
    XSSimpleType getType();

    String getDefaultValue();
    String getFixedValue();

    /**
     * Gets the context in which the default/fixed value
     * constraint should be interpreted.
     * 
     * <p>
     * The primary use of the ValidationContext is to resolve the
     * namespace prefix of the value constraint when it is a QName.
     */
    ValidationContext getContext();
}
