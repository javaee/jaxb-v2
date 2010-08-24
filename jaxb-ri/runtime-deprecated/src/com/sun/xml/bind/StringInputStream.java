/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.bind;

import java.io.InputStream;

/**
 * Same as {@link java.io.StringBufferInputStream}.
 * 
 * If we use the StringBufferInputStream class in the generated code,
 * it will cause warnings. That's why this class is re-implemented
 * 
 * @since 1.0
 */
// TODO: but I realized that this class will not be used directly from
// the generated code. So I guess warnings are deemd acceptable.
// shall I keep this class, or shall I just use java.io.StringBufferInputStream?
final public class StringInputStream extends InputStream
{
    private final String str;
    private int idx=0;
    
    public StringInputStream( String _str ) { this.str=_str; }
    
    public int available() { return str.length()-idx; }
    public int read() {
        if(idx==str.length())   return -1;
        return str.charAt(idx++);
    }
    public int read(byte[] buf) {
        return read(buf,0,buf.length);
    }
    
    public int read(byte[] buf, int offset, int len ) {
        if(idx==str.length())   return -1;
        
        len = Math.min( len, available() );
        for( int i=0; i<len; i++ )
            buf[i+offset] = (byte)str.charAt(idx++);
        
        return len;
    }
}
