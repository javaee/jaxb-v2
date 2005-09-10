/*
 * @(#)$Id: MinimumEscapeHandler.java,v 1.2 2005-09-10 19:07:39 kohsuke Exp $
 */

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

/**
 * Performs no character escaping. Usable only when the output encoding
 * is UTF, but this handler gives the maximum performance.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class MinimumEscapeHandler implements CharacterEscapeHandler {
    
    private MinimumEscapeHandler() {}  // no instanciation please
    
    public static final CharacterEscapeHandler theInstance = new MinimumEscapeHandler(); 
    
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        // avoid calling the Writerwrite method too much by assuming
        // that the escaping occurs rarely.
        // profiling revealed that this is faster than the naive code.
        int limit = start+length;
        for (int i = start; i < limit; i++) {
            char c = ch[i];
            if( c=='&' || c=='<' || c=='>' || (c=='\"' && isAttVal) ) {
                if(i!=start)
                    out.write(ch,start,i-start);
                start = i+1;
                switch (ch[i]) {
                case '&' :
                    out.write("&amp;");
                    break;
                case '<' :
                    out.write("&lt;");
                    break;
                case '>' :
                    out.write("&gt;");
                    break;
                case '\"' :
                    out.write("&quot;");
                    break;
                }
            }
        }
        
        if( start!=limit )
            out.write(ch,start,limit-start);
    }

}
