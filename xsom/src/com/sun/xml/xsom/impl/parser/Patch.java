/*
 * @(#)$Id: Patch.java,v 1.1 2005-04-14 22:06:30 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.parser;

import org.xml.sax.SAXException;

/**
 * Patch program that runs later to "fix" references among components.
 * 
 * The only difference from the Runnable interface is that this interface
 * allows the program to throw a SAXException.
 */
public interface Patch {
    void run() throws SAXException;
}

