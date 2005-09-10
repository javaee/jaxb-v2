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
import java.io.PrintStream;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

/**
 * Output all source files into a single stream with a little
 * formatting header in front of each file.
 * 
 * This is primarily for human consumption of the generated source
 * code, such as to debug/test CodeModel or to quickly inspect the result.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SingleStreamCodeWriter extends CodeWriter {
    
    private final PrintStream out;
    
    /**
     * @param os
     *      This stream will be closed at the end of the code generation.
     */
    public SingleStreamCodeWriter( OutputStream os ) {
        out = new PrintStream(os);
    }

    public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
        String pkgName = pkg.name();
        if(pkgName.length()!=0)     pkgName += '.';
        
        out.println(
            "-----------------------------------" + pkgName+fileName +
            "-----------------------------------");
            
        return new FilterOutputStream(out) {
            public void close() {
                // don't let this stream close
            }
        };
    }

    public void close() throws IOException {
        out.close();
    }

}
