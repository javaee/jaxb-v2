/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.codemodel.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * {@link Writer} that escapes non US-ASCII characters into
 * Java Unicode escape \\uXXXX.
 * 
 * This process is necessary if the method names or field names
 * contain non US-ASCII characters.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class UnicodeEscapeWriter extends FilterWriter {
    
    public UnicodeEscapeWriter( Writer next ) {
        super(next);
    }

    public final void write(int ch) throws IOException {
        if(!requireEscaping(ch))  out.write(ch);
        else {
            // need to escape
            out.write("\\u");
            String s = Integer.toHexString(ch);
            for( int i=s.length(); i<4; i++ )
                out.write('0');
            out.write(s);
        }
    }

    /**
     * Can be overrided. Return true if the character
     * needs to be escaped. 
     */
    protected boolean requireEscaping(int ch) {
        if(ch>=128)     return true;
        
        // control characters
        if( ch<0x20 && " \t\r\n".indexOf(ch)==-1 )  return true;
        
        return false;
    }
    
    public final void write(char[] buf, int off, int len) throws IOException {
        for( int i=0; i<len; i++ )
            write(buf[off+i]);
    }

    public final void write(char[] buf) throws IOException {
        write(buf,0,buf.length);
    }

    public final void write(String buf, int off, int len) throws IOException {
        write( buf.toCharArray(), off, len );
    }
    
    public final void write(String buf) throws IOException {
        write( buf.toCharArray(), 0, buf.length() );
    }

}
