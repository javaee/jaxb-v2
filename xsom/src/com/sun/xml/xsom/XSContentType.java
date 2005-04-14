/*
 * @(#)$Id: XSContentType.java,v 1.1 2005-04-14 22:06:19 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;

/**
 * Content of a complex type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSContentType extends XSComponent
{
    /**
     * Equivalent of <code>(this instanceof XSSimpleType)?this:null</code>
     */
    XSSimpleType asSimpleType();
    /**
     * Equivalent of <code>(this instanceof XSParticle)?this:null</code>
     */
    XSParticle asParticle();
    /**
     * If this content type represents the empty content, return <code>this</code>,
     * otherwise null.
     */
    XSContentType asEmpty();

    <T> T apply( XSContentTypeFunction<T> function );
    void visit( XSContentTypeVisitor visitor );
}
