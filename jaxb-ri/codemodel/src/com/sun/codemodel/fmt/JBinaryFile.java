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
package com.sun.codemodel.fmt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.codemodel.JResourceFile;

/**
 * Allows the application to use OutputStream to define data
 * that will be stored into a file.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class JBinaryFile extends JResourceFile {
    
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    public JBinaryFile( String name ) {
        super(name);
    }
    
    /**
     * 
     * @return
     *      Data written to the returned output stream will be written
     *      to the file.
     */
    public OutputStream getDataStore() {
        return baos;
    }
    
    public void build(OutputStream os) throws IOException {
        os.write( baos.toByteArray() );
    }
}
