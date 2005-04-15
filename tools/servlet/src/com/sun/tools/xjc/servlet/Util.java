/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Util {
    /**
     * Copy all the bytes from input to output, but don't close the streams.
     */
    public static void copyStream( OutputStream out, InputStream in ) throws IOException {
        byte[] buf = new byte[256];
        int len;
        while((len=in.read(buf))!=-1)
            out.write(buf,0,len);
        out.flush();
    }
}
