/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Receives generated code and writes to the appropriate storage.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface CodeWriter {
    
    /**
     * Called by CodeModel to store the specified file.
     * The callee must allocate a storage to store the specified file.
     * 
     * <p>
     * The returned stream will be closed before the next file is
     * stored. So the callee can assume that only one OutputStream
     * is active at any given time.
     * 
     * @param   pkg
     *      The package of the file to be written.
     * @param   fileName
     *      File name without the path. Something like
     *      "Foo.java" or "Bar.properties"
     */
    OutputStream open( JPackage pkg, String fileName ) throws IOException;
    
    /**
     * Called by CodeModel at the end of the process.
     */
    void close() throws IOException;
}
