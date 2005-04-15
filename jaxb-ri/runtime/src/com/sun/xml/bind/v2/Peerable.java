/*
 * @(#)$Id: Peerable.java,v 1.1 2005-04-15 20:03:54 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.bind.v2;

/**
 * Enum that represents the possible peer-ablility.
 * 
 * <p>
 * I thought about making it a type-safe enum, but felt
 * that a number constant would probably be faster
 * (if ever so slightly.)
 * 
 * @since 2.0
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class Peerable {
    private Peerable() {}
    
    public static final byte NONE  = 0;
    public static final byte INNER = 1;
    public static final byte OUTER = 2;
    /**
     * Leaf objects are immutable objects, and as such
     * they don't participate in the binder activity.
     */
    public static final byte IMMUTABLE = 3;
}
