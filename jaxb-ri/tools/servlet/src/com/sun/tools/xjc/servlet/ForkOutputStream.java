/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Sends the same byte stream to two streams.
 * Just like UNIX "tee" tool.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ForkOutputStream extends OutputStream {
    
    private final OutputStream lhs,rhs;
    
    public ForkOutputStream( OutputStream lhs, OutputStream rhs ) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
    
    public void close() throws IOException {
        lhs.close();
        rhs.close();
    }

    public void flush() throws IOException {
        lhs.flush();
        rhs.flush();
    }

    public void write(byte[] b) throws IOException {
        lhs.write(b);
        rhs.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        lhs.write(b, off, len);
        rhs.write(b, off, len);
    }

    public void write(int b) throws IOException {
        lhs.write(b);
        rhs.write(b);
    }

}
