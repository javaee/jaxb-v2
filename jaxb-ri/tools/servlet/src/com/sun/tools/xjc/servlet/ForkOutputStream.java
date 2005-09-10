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
