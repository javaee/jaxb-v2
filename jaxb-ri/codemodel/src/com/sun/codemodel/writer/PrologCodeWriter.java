/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * Writes all the source files under the specified file folder and 
 * inserts a file prolog comment in each java source file.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PrologCodeWriter implements CodeWriter {
    
    private final CodeWriter core;
    
    /** prolog comment */
    private final String prolog;
    
    /**
     * @param core
     *      This CodeWriter will be used to actually create a storage for files.
     *      PrologCodeWriter simply decorates this underlying CodeWriter by
     *      adding prolog comments.
     * @param prolog
     *      Strings that will be added as comments.
     *      This string may contain newlines to produce multi-line comments.
     *      '//' will be inserted at the beginning of each line to make it
     *      a valid Java comment, so the caller can just pass strings like
     *      "abc\ndef" 
     */
    public PrologCodeWriter( CodeWriter core, String prolog ) throws IOException {
        this.core = core;
        this.prolog = prolog;
    }
    
    
    public OutputStream open(JPackage pkg, String fileName) throws IOException {
        OutputStream fos = core.open(pkg,fileName);
        
        PrintWriter out = new PrintWriter(fos);
        
        // write prolog if this is a java source file
        if( ( prolog != null ) && ( fileName.endsWith( ".java" ) ) ) {
            out.println( "//" );
            
            String s = prolog;
            int idx;
            while( (idx=s.indexOf('\n'))!=-1 ) {
                out.println("// "+ s.substring(0,idx) );
                s = s.substring(idx+1);
            }
            out.println("//");
            out.println();
        }
        out.flush();    // we can't close the stream for that would close the undelying stream.
        
        return fos;
    }
    
    public void close() throws IOException {
        core.close();
    }
}
