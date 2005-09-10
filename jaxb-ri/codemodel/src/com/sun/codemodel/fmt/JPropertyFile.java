/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
