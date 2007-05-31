/*
 * @(#)$Id: MinimumEscapeHandler.java,v 1.2.6.1 2007-05-31 21:58:55 ofung Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
