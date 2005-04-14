/*
 * @(#)$Id: XSContentTypeFunction.java,v 1.1 2005-04-14 22:06:38 kohsuke Exp $
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
 * Function object that works on {@link XSContentType}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSContentTypeFunction<T> {
    T simpleType( XSSimpleType simpleType );
    T particle( XSParticle particle );
    T empty( XSContentType empty );
}

