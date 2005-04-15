/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.fmt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.JResourceFile;

/**
 * Allows the application to use OutputStream to define data
 * that will be stored into a file.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class JBinaryFile extends JResourceFile {
    
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    public JBinaryFile( String name ) {
        super(name);
    }
    
    /**
     * 
     * @return
     *      Data written to the returned output stream will be written
     *      to the file.
     */
    public OutputStream getDataStore() {
        return baos;
    }
    
    public void build(OutputStream os) throws IOException {
        os.write( baos.toByteArray() );
    }
}
