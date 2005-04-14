/*
 * @(#)$Id: XSTermFunction.java,v 1.1 2005-04-14 22:06:39 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
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
import com.sun.xml.xsom.XSTerm;

/**
 * Function object that works on {@link XSTerm}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSTermFunction<T> {
    T wildcard( XSWildcard wc );
    T modelGroupDecl( XSModelGroupDecl decl );
    T modelGroup( XSModelGroup group );
    T elementDecl( XSElementDecl decl );
}

