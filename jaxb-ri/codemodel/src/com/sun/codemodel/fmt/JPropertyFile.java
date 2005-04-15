/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.fmt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import com.sun.codemodel.JResourceFile;

/**
 * A property file.
 */
public class JPropertyFile extends JResourceFile
{
    public JPropertyFile( String name ) {
        super(name);
    }
    
    private final Properties data = new Properties();
    
    /**
     * Adds key/value pair into the property file.
     * If you call this method twice with the same key,
     * the old one is overriden by the new one.
     */
    public void add( String key, String value ) {
        data.put(key,value);
    }
    
    // TODO: method to iterate values in data?
    // TODO: should we rather expose Properties object directly via
    // public Properties body() { return data; } ?
    
    public void build( OutputStream out ) throws IOException {
        data.store(out,null);
    }
}
