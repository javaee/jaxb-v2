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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Unzips the contents of a zip file into a specified directory.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Unzipper {
    private final File outDir;

    public Unzipper( File outDir ) {
        this.outDir = outDir;
    }
    
    public void unzip( InputStream input ) throws IOException {
        ZipInputStream zip = new ZipInputStream(input);
        while(true) {
            ZipEntry ze = zip.getNextEntry();
            if(ze==null)    break;
            
            procsesEntry(zip,ze);
            
            zip.closeEntry();
        }
        zip.close();
    }

    private void procsesEntry(ZipInputStream zip, ZipEntry ze) throws IOException {
        String name = ze.getName();
        System.out.println(name);
        
        if( name.indexOf("/impl/")!=-1 || name.indexOf("\\impl\\")!=-1 )
            return; // skip implementation
        
        if( name.endsWith("/") || name.endsWith("\\") ) {
            // directory
            return;
        }
        
        if( !name.endsWith(".java") ) return;
        
        
        File item = new File(outDir,name);
        item.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(item);
        
        Util.copyStream( out, zip );
        out.close();
    }
}
