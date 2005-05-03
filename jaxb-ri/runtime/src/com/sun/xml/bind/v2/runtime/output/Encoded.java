package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Buffer for UTF-8 encoded string.
 *
 * See http://www.cl.cam.ac.uk/~mgk25/unicode.html#utf-8 for the UTF-8 encoding.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Encoded {
    public byte[] buf;

    public int len;

    public Encoded() {}

    public Encoded(String text) {
        set(text);
    }

    public void ensureSize(int size) {
        if(buf==null || buf.length<size)
            buf = new byte[size];
    }

    public final void set( String text ) {
        int length = text.length();

        ensureSize(length*3+1); // +1 for append

        int ptr = 0;

        for (int i = 0; i < length; i++) {
            final char chr = text.charAt(i);
            if (chr > 0x7F) {
                if (chr > 0x7FF) {
                    buf[ptr++] = (byte)(0xE0 + (chr >> 12));
                    buf[ptr++] = (byte)(0x80 + ((chr >> 6) & 0x3F));
                } else {
                    buf[ptr++] = (byte)(0xC0 + (chr >> 6));
                }
                buf[ptr++] = (byte)(0x80 + (chr & 0x3F));
            } else {
                buf[ptr++] = (byte)chr;
            }
        }

        len = ptr;
    }

    /**
     * Fill in the buffer by encoding the specified characters
     * while escaping characters like &lt;
     */
    public final void setEscape(char[] text, int length) {
        ensureSize(length*6+1);     // in the worst case the text is like """""", so we need 6 bytes per char

        int ptr = 0;

        for (int i = 0; i < length; i++) {
            final char chr = text[i];
            if (chr > 0x7F) {
                if (chr > 0x7FF) {
                    buf[ptr++] = (byte)(0xE0 + (chr >> 12));
                    buf[ptr++] = (byte)(0x80 + ((chr >> 6) & 0x3F));
                } else {
                    buf[ptr++] = (byte)(0xC0 + (chr >> 6));
                }
                buf[ptr++] = (byte)(0x80 + (chr & 0x3F));
            } else {
                switch(chr) {
                case '&':
                    ptr = writeEntity(AMP_ENTITY,ptr);
                    break;
                case '<':
                    ptr = writeEntity(AMP_LT,ptr);
                    break;
                case '>':
                    ptr = writeEntity(AMP_GT,ptr);
                    break;
                case '"':
                    ptr = writeEntity(AMP_QUOT,ptr);
                    break;
                default:
                    buf[ptr++] = (byte)chr;
                }
            }
        }

        len = ptr;
    }

    /**
     * Fill in the buffer by encoding the specified characters
     * while escaping characters like &lt;
     */
    public final void setEscape(CharSequence text) {
        int length = text.length();
        ensureSize(length*6+1);     // in the worst case the text is like """""", so we need 6 bytes per char

        int ptr = 0;

        for (int i = 0; i < length; i++) {
            final char chr = text.charAt(i);
            if (chr > 0x7F) {
                if (chr > 0x7FF) {
                    buf[ptr++] = (byte)(0xE0 + (chr >> 12));
                    buf[ptr++] = (byte)(0x80 + ((chr >> 6) & 0x3F));
                } else {
                    buf[ptr++] = (byte)(0xC0 + (chr >> 6));
                }
                buf[ptr++] = (byte)(0x80 + (chr & 0x3F));
            } else {
                switch(chr) {
                case '&':
                    ptr = writeEntity(AMP_ENTITY,ptr);
                    break;
                case '<':
                    ptr = writeEntity(AMP_LT,ptr);
                    break;
                case '>':
                    ptr = writeEntity(AMP_GT,ptr);
                    break;
                case '"':
                    ptr = writeEntity(AMP_QUOT,ptr);
                    break;
                default:
                    buf[ptr++] = (byte)chr;
                }
            }
        }

        len = ptr;
    }

    private int writeEntity( byte[] entity, int ptr ) {
        System.arraycopy(entity,0,buf,ptr,entity.length);
        return ptr+entity.length;
    }

    /**
     * Writes the encoded bytes to the given output stream.
     */
    public final void write(OutputStream out) throws IOException {
        out.write(buf,0,len);
    }

    /**
     * Appends a new character to the end of the buffer.
     * This assumes that you have enough space in the buffer.
     */
    public void append(char b) {
        buf[len++] = (byte)b;
    }

    /**
     * Reallocate the buffer to the exact size of the data
     * to reduce the memory footprint.
     */
    public void compact() {
        byte[] b = new byte[len];
        System.arraycopy(buf,0,b,0,len);
        buf = b;
    }

    private static final byte[] AMP_ENTITY = UTF8XmlOutput.toBytes("&amp;");
    private static final byte[] AMP_GT = UTF8XmlOutput.toBytes("&gt;");
    private static final byte[] AMP_LT = UTF8XmlOutput.toBytes("&lt;");
    private static final byte[] AMP_QUOT = UTF8XmlOutput.toBytes("&quot;");
}
