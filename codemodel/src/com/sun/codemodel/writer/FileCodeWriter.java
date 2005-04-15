/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * Writes all the source files under the specified file folder.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class FileCodeWriter implements CodeWriter {

    /** The target directory to put source code. */
    private final File target;
    
    /** specify whether or not to mark the generated files read-only */
    private final boolean readOnly;

    /** Files that shall be marked as read only. */
    private final Set readonlyFiles = new HashSet();
    
    public FileCodeWriter( File target ) throws IOException {
        this(target,false);
    }
    
    public FileCodeWriter( File target, boolean readOnly ) throws IOException {
        this.target = target;
        this.readOnly = readOnly;
        if(!target.exists() || !target.isDirectory())
            throw new IOException(target + ": non-existent directory");
    }
    
    
    public OutputStream open(JPackage pkg, String fileName) throws IOException {
        return new FileOutputStream(getFile(pkg,fileName));
    }
    
    protected File getFile(JPackage pkg, String fileName ) throws IOException {
        File dir;
        if(pkg.isUnnamed())
            dir = target;
        else
            dir = new File(target, toDirName(pkg));
        
        if(!dir.exists())   dir.mkdirs();
        
        File fn = new File(dir,fileName);
        
        if (fn.exists()) {
            if (!fn.delete())
                throw new IOException(fn + ": Can't delete previous version");
        }
        
        
        if(readOnly)        readonlyFiles.add(fn);
        return fn;
    }

    public void close() throws IOException {
        // mark files as read-onnly if necessary
        for (Iterator itr = readonlyFiles.iterator(); itr.hasNext();) {
            File f = (File) itr.next();
            f.setReadOnly();
        }
    }
    
    /** Converts a package name to the directory name. */
    private String toDirName( JPackage pkg ) {
        return pkg.name().replace('.',File.separatorChar);
    }

}
