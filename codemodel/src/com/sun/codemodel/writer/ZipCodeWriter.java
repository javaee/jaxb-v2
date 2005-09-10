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
public class ZipCodeWriter extends CodeWriter {
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
        
    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        String name = fileName;
        if(!pkg.isUnnamed())    name = toDirName(pkg)+name;
        
        zip.putNextEntry(new ZipEntry(name));
        return filter;
    }

    /** Converts a package name to the directory name. */
    private static String toDirName( JPackage pkg ) {
        return pkg.name().replace('.','/')+'/';
    }

    public void close() throws IOException {
        zip.close();
    }

}
