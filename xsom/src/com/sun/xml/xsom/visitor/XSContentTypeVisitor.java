/*
 * @(#)$Id: XSContentTypeVisitor.java,v 1.1 2005-04-14 22:06:39 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;

/**
 * Visitor that works on {@link XSContentType}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSContentTypeVisitor {
    void simpleType( XSSimpleType simpleType );
    void particle( XSParticle particle );
    void empty( XSContentType empty );
}
