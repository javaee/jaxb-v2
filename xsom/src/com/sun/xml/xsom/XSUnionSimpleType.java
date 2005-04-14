/*
 * @(#)$Id: XSUnionSimpleType.java,v 1.1 2005-04-14 22:06:22 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

/**
 * Union simple type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSUnionSimpleType extends XSSimpleType
{
    XSSimpleType getMember(int idx);
    int getMemberSize();
}
