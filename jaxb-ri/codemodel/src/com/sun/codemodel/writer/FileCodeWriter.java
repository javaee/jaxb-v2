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
package com.sun.codemodel.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * Writes all the source files under the specified file folder.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class FileCodeWriter extends CodeWriter {

    /** The target directory to put source code. */
    private final File target;
    
    /** specify whether or not to mark the generated files read-only */
    private final boolean readOnly;

    /** Files that shall be marked as read only. */
    private final Set<File> readonlyFiles = new HashSet<File>();
    
    public FileCodeWriter( File target ) throws IOException {
        this(target,false);
    }
    
    public FileCodeWriter( File target, boolean readOnly ) throws IOException {
        this.target = target;
        this.readOnly = readOnly;
        if(!target.exists() || !target.isDirectory())
            throw new IOException(target + ": non-existent directory");
    }
    
    
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
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
        for (File f : readonlyFiles)
            f.setReadOnly();
    }
    
    /** Converts a package name to the directory name. */
    private static String toDirName( JPackage pkg ) {
        return pkg.name().replace('.',File.separatorChar);
    }

}
