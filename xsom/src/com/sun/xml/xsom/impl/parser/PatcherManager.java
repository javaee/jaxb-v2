/*
 * @(#)$Id: PatcherManager.java,v 1.1 2005-04-14 22:06:30 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl.parser;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Manages patchers.
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface PatcherManager {
    void addPatcher( Patch p );
    /**
     * Reports an error during the parsing.
     * 
     * @param source
     *      location of the error in the source file, or null if
     *      it's unavailable.
     */
    void reportError( String message, Locator source ) throws SAXException;
    
    
    public interface Patcher {
        void run() throws SAXException;
    }
}
