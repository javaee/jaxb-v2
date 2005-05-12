/*
 * @(#)$Id: XSUnionSimpleType.java,v 1.2 2005-05-12 04:11:37 kohsuke Exp $
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
public interface XSUnionSimpleType extends XSSimpleType, Iterable<XSSimpleType>
{
    XSSimpleType getMember(int idx);
    int getMemberSize();
}
