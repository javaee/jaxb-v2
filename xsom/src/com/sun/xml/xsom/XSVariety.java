/*
 * @(#)$Id: XSVariety.java,v 1.1 2005-04-14 22:06:22 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom;

/**
 * Constants that represent variety of simple types.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke,kawaguchi@sun.com)
 */
public final class XSVariety {
    public static final XSVariety ATOMIC = new XSVariety("atomic");
    public static final XSVariety UNION  = new XSVariety("union");
    public static final XSVariety LIST   = new XSVariety("list");
    
    private XSVariety(String _name) { this.name=_name; }
    private final String name;
    public String toString() { return name; }
}

