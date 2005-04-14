/*
 * @(#)$Id: XSParticle.java,v 1.1 2005-04-14 22:06:21 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

/**
 * Particle schema component.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSParticle extends XSContentType
{
    int getMinOccurs();
    /**
     * Gets the max occurs property.
     * 
     * @return
     *      {@link UNBOUNDED} will be returned if the value
     *      is "unbounded".
     */
    int getMaxOccurs();

    public static final int UNBOUNDED = -1;

    XSTerm getTerm();
}
