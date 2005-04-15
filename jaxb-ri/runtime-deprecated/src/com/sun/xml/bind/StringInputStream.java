/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
