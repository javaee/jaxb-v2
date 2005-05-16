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
     * The meat of {@link #setEscape}. The VM should be able to inline this.
     */
    private final int escapeChar(boolean isAttribute, final char chr, int ptr) {
        if (chr > 0x7F) {
            if (chr > 0x7FF) {
                buf[ptr++] = (byte)(0xE0 + (chr >> 12));
                buf[ptr++] = (byte)(0x80 + ((chr >> 6) & 0x3F));
            } else {
                buf[ptr++] = (byte)(0xC0 + (chr >> 6));
            }
            buf[ptr++] = (byte)(0x80 + (chr & 0x3F));
        } else {
            byte[] ent;

            if((ent=attributeEntities[chr])!=null) {
                // the majority of the case is just printed as a char,
                // so it's very important to reject them as quickly as possible

                // check again to see if this really needs to be escaped
                if(isAttribute || entities[chr]!=null)
                    ptr = writeEntity(ent,ptr);
                else
                    buf[ptr++] = (byte)chr;
            } else
                buf[ptr++] = (byte)chr;
        }
        return ptr;
    }

    /**
     * Fill in the buffer by encoding the specified characters
     * while escaping characters like &lt;
     */
    public final void setEscape(char[] text, int length, boolean isAttribute) {
        ensureSize(length*6+1);     // in the worst case the text is like """""", so we need 6 bytes per char

        int ptr = 0;

        for (int i = 0; i < length; i++)
            ptr = escapeChar(isAttribute,text[i],ptr);
        len = ptr;
    }


    /**
     * Fill in the buffer by encoding the specified characters
     * while escaping characters like &lt;
     *
     * @param isAttribute
     *      if true, characters like \t, \r, and \n are also escaped.
     */
    public final void setEscape(CharSequence text, boolean isAttribute) {
        int length = text.length();
        ensureSize(length*6+1);     // in the worst case the text is like """""", so we need 6 bytes per char

        int ptr = 0;

        for (int i = 0; i < length; i++)
            ptr = escapeChar(isAttribute,text.charAt(i),ptr);
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

    /**
     * UTF-8 encoded entities keyed by their character code.
     * e.g., entities['&'] == AMP_ENTITY.
     *
     * In attributes we need to encode more characters.
     */
    private static final byte[][] entities = new byte[0x80][];
    private static final byte[][] attributeEntities = new byte[0x80][];

    static {
        add('&',"&amp;",false);
        add('<',"&lt;",false);
        add('>',"&gt;",false);
        add('"',"&quot;",false);
        add('\t',"&#x9;",true);
        add('\r',"&#xD;",true);
        add('\n',"&#xA;",true);
    }

    private static void add(char c, String s, boolean attOnly) {
        byte[] image = UTF8XmlOutput.toBytes(s);
        attributeEntities[c] = image;
        if(!attOnly)
            entities[c] = image;
    }
}
