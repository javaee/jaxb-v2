/*
 * @(#)$Id: XSListSimpleType.java,v 1.1 2005-04-14 22:06:20 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

/**
 * List simple type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSListSimpleType extends XSSimpleType
{
    XSSimpleType getItemType();
}
