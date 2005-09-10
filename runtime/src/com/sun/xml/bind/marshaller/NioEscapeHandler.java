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
package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Uses JDK1.4 NIO functionality to escape characters smartly.
 * 
 * @since 1.0.1
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NioEscapeHandler implements CharacterEscapeHandler {
    
    private final CharsetEncoder encoder;
    
    // exposing those variations upset javac 1.3.1, since it needs to
    // know about those classes to determine which overloaded version
    // of the method it wants to use. So comment it out for the compatibility.
    
//    public NioEscapeHandler(CharsetEncoder _encoder) {
//        this.encoder = _encoder;
//        if(encoder==null)
//            throw new NullPointerException();
//    }
//
//    public NioEscapeHandler(Charset charset) {
//        this(charset.newEncoder());
//    }
    
    public NioEscapeHandler(String charsetName) {
//        this(Charset.forName(charsetName));
        this.encoder = Charset.forName(charsetName).newEncoder(); 
    }
    
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        int limit = start+length;
        for (int i = start; i < limit; i++) {
            switch (ch[i]) {
            case '&':
                out.write("&amp;");
                break;
            case '<':
                out.write("&lt;");
                break;
            case '>':
                out.write("&gt;");
                break;
            case '\"':
                if (isAttVal) {
                    out.write("&quot;");
                } else {
                    out.write('\"');
                }
                break;
            default:
                if( encoder.canEncode(ch[i]) ) {
                    out.write(ch[i]);
                } else {
                    out.write("&#");
                    out.write(Integer.toString(ch[i]));
                    out.write(';');
                }
            }
        }
    }

}
