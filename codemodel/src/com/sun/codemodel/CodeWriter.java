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
package com.sun.codemodel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.CharsetEncoder;

import com.sun.codemodel.util.EncoderFactory;
import com.sun.codemodel.util.UnicodeEscapeWriter;

/**
 * Receives generated code and writes to the appropriate storage.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class CodeWriter {
    
    /**
     * Called by CodeModel to store the specified file.
     * The callee must allocate a storage to store the specified file.
     * 
     * <p>
     * The returned stream will be closed before the next file is
     * stored. So the callee can assume that only one OutputStream
     * is active at any given time.
     * 
     * @param   pkg
     *      The package of the file to be written.
     * @param   fileName
     *      File name without the path. Something like
     *      "Foo.java" or "Bar.properties"
     */
    public abstract OutputStream openBinary( JPackage pkg, String fileName ) throws IOException;

    /**
     * Called by CodeModel to store the specified file.
     * The callee must allocate a storage to store the specified file.
     *
     * <p>
     * The returned stream will be closed before the next file is
     * stored. So the callee can assume that only one OutputStream
     * is active at any given time.
     *
     * @param   pkg
     *      The package of the file to be written.
     * @param   fileName
     *      File name without the path. Something like
     *      "Foo.java" or "Bar.properties"
     */
    public Writer openSource( JPackage pkg, String fileName ) throws IOException {
        final OutputStreamWriter bw = new OutputStreamWriter(openBinary(pkg,fileName));

        // create writer
        try {
            return new UnicodeEscapeWriter(bw) {
                // can't change this signature to Encoder because
                // we can't have Encoder in method signature
                private final CharsetEncoder encoder = EncoderFactory.createEncoder(bw.getEncoding());
                protected boolean requireEscaping(int ch) {
                    // control characters
                    if( ch<0x20 && " \t\r\n".indexOf(ch)==-1 )  return true;
                    // check ASCII chars, for better performance
                    if( ch<0x80 )       return false;

                    return !encoder.canEncode((char)ch);
                }
            };
        } catch( Throwable t ) {
            return new UnicodeEscapeWriter(bw);
        }
    }

    /**
     * Called by CodeModel at the end of the process.
     */
    public abstract void close() throws IOException;
}
