/*
 * @(#)$Id: XSTermVisitor.java,v 1.1 2005-04-14 22:06:40 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSWildcard;

/**
 * Visitor that works on {@link com.sun.xml.xsom.XSTerm}.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSTermVisitor {
    void wildcard( XSWildcard wc );
    void modelGroupDecl( XSModelGroupDecl decl );
    void modelGroup( XSModelGroup group );
    void elementDecl( XSElementDecl decl );
}
