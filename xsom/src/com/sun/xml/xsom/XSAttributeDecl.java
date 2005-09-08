/*
 * @(#)$Id: XSAttributeDecl.java,v 1.2 2005-09-08 22:49:31 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

/**
 * Attribute declaration.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSAttributeDecl extends XSDeclaration
{
    XSSimpleType getType();

    XmlString getDefaultValue();
    XmlString getFixedValue();
}
