/*
 * @(#)$Id: XSSimpleTypeVisitor.java,v 1.1 2005-04-14 22:06:39 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;

/**
 * Visitor that works on {@link com.sun.xml.xsom.XSSimpleType}
 * and its derived interfaces.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public interface XSSimpleTypeVisitor {
    void listSimpleType( XSListSimpleType type );
    void unionSimpleType( XSUnionSimpleType type );
    void restrictionSimpleType( XSRestrictionSimpleType type );
}
