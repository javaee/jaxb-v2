/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.writer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * Filter CodeWriter that writes a progress message to the specified
 * PrintStream.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ProgressCodeWriter implements CodeWriter {
    public ProgressCodeWriter( CodeWriter output, PrintStream progress ) {
        this.output = output;
        this.progress = progress;
        if(progress==null)
            throw new IllegalArgumentException();
    }

    private final CodeWriter output;
    private final PrintStream progress;
    
    public OutputStream open(JPackage pkg, String fileName) throws IOException {
        if(pkg.isUnnamed()) progress.println(fileName);
        else
            progress.println(
                pkg.name().replace('.',File.separatorChar)
                    +File.separatorChar+fileName);
        
        return output.open(pkg,fileName);
    }
    
    public void close() throws IOException {
        output.close();
    }

}
