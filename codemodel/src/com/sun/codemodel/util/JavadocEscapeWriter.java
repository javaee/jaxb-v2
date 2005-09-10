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
package com.sun.codemodel.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * {@link Writer} that escapes characters that are unsafe
 * as Javadoc comments.
 * 
 * Such characters include '&lt;' and '&amp;'.
 * 
 * <p>
 * Note that this class doesn't escape other Unicode characters
 * that are typically unsafe. For example, &#x611B; (A kanji
 * that means "love") can be considered as unsafe because
 * javac with English Windows cannot accept this character in the
 * source code.
 * 
 * <p>
 * If the application needs to escape such characters as well, then
 * they are on their own.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JavadocEscapeWriter extends FilterWriter {
    
    public JavadocEscapeWriter( Writer next ) {
        super(next);
    }

    public void write(int ch) throws IOException {
        if(ch=='<')
            out.write("&lt;");
        else
        if(ch=='&')
            out.write("&amp;");
        else
            out.write(ch);
    }
    
    public void write(char[] buf, int off, int len) throws IOException {
        for( int i=0; i<len; i++ )
            write(buf[off+i]);
    }

    public void write(char[] buf) throws IOException {
        write(buf,0,buf.length);
    }

    public void write(String buf, int off, int len) throws IOException {
        write( buf.toCharArray(), off, len );
    }
    
    public void write(String buf) throws IOException {
        write( buf.toCharArray(), 0, buf.length() );
    }

}
