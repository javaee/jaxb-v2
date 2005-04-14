/*
 * @(#)$Id: XSNotation.java,v 1.1 2005-04-14 22:06:20 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

/**
 * Notation declaration.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSNotation extends XSDeclaration {
    String getPublicId();
    String getSystemId();
}
