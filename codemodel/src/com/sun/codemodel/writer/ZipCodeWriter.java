/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.writer;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * Writes all the files into a zip file.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ZipCodeWriter implements CodeWriter {
    /**
     * @param target
     *      Zip file will be written to this stream.
     */
    public ZipCodeWriter( OutputStream target ) {
        zip = new ZipOutputStream(target);
        // nullify the close method.
        filter = new FilterOutputStream(zip){
            public void close() {}
        };
    }
    
    private final ZipOutputStream zip;
    
    private final OutputStream filter;
        
    public OutputStream open(JPackage pkg, String fileName) throws IOException {
        String name = fileName;
        if(!pkg.isUnnamed())    name = toDirName(pkg)+name;
        
        zip.putNextEntry(new ZipEntry(name));
        return filter;
    }

    /** Converts a package name to the directory name. */
    private String toDirName( JPackage pkg ) {
        return pkg.name().replace('.','/')+'/';
    }

    public void close() throws IOException {
        zip.close();
    }

}
