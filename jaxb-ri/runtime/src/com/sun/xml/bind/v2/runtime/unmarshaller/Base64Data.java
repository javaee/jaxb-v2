package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

/**
 * Fed to unmarshaller when the 'text' data is actually
 * a virtual image of base64 encoding of the binary data
 * transferred on the wire.
 *
 * Used for the MTOM support.
 *
 * This object is mutable and the owner of this object can
 * reuse it with new data.
 *
 * @see XmlVisitor#text(CharSequence)
 * @see XMLSerializer#text(CharSequence,String)
 *
 * @author Kohsuke Kawaguchi
 */
public final class Base64Data implements CharSequence {

    // either dataHandler or data must be present

    private DataHandler dataHandler;

    private byte[] data;
    /**
     * Length of the valid data in {@link #data}.
     */
    private int dataLen;

    public void set(byte[] data, int len) {
        this.data = data;
        this.dataLen = len;
        this.dataHandler = null;
    }

    public void set(byte[] data) {
        set(data,data.length);
    }


    public void set(DataHandler data) {
        assert data!=null;
        this.dataHandler = data;
        this.data = null;
    }

    /**
     * Gets the raw data.
     */
    public DataHandler getData() {
        if(dataHandler==null)
            // TODO
            throw new UnsupportedOperationException();
        return dataHandler;
    }

    /**
     * Gets the byte[] of the exact length.
     */
    public byte[] getExact() {
        get();
        if(dataLen!=data.length) {
            byte[] buf = new byte[dataLen];
            System.arraycopy(data,0,buf,0,dataLen);
            data = buf;
        }
        return data;
    }

    /**
     * Gets the data as an {@link InputStream}.
     */
    public InputStream getInputStream() throws IOException {
        if(dataHandler!=null)
            return dataHandler.getInputStream();
        else
            return new ByteArrayInputStream(data,0,dataLen);
    }

    /**
     * Gets the raw data.
     */
    public byte[] get() {
        if(data==null) {
            try {
                data = new byte[1024];
                InputStream is = dataHandler.getDataSource().getInputStream();
                int offset=0;
                dataLen=0;

                while(true) {
                    int len = is.read(data,offset,data.length-offset);
                    if(len<0)   break;

                    dataLen += len;
                    if(dataLen==data.length) {
                        byte[] buf = new byte[data.length*2];
                        System.arraycopy(data,0,buf,0,data.length);
                        data = buf;
                    }
                }
            } catch (IOException e) {
                // TODO: report the error to the unmarshaller
                dataLen = 0;    // recover by assuming length-0 data
            }
        }
        return data;
    }

    public int length() {
        // for each 3 bytes you use 4 chars
        // if the remainder is 1 or 2 there will be 4 more
        get();  // fill in the buffer if necessary
        return ((dataLen+2)/3)*4;
    }

    public char charAt(int index) {
        // we assume that the length() method is called before this method
        // (otherwise how would the caller know that the index is valid?)
        // so we assume that the byte[] is already populated

        int offset = index%4;
        int base = (index/4)*3;

        byte b1,b2;

        switch(offset) {
        case 0:
            return DatatypeConverterImpl.encode(data[base]>>2);
        case 1:
            if(base+1<dataLen)
                b1 = data[base+1];
            else
                b1 = 0;
            return DatatypeConverterImpl.encode(
                        ((data[base]&0x3)<<4) |
                        ((b1>>4)&0xF));
        case 2:
            if(base+1<dataLen) {
                b1 = data[base+1];
                if(base+2<dataLen)
                    b2 = data[base+2];
                else
                    b2 = 0;

                return DatatypeConverterImpl.encode(
                            ((b1&0xF)<<2)|
                            ((b2>>6)&0x3));
            } else
                return '=';
        case 3:
            if(base+2<dataLen)
                return DatatypeConverterImpl.encode(data[base+2]&0x3F);
            else
                return '=';
        }

        throw new IllegalStateException();
    }

    /**
     * Internally this is only used to split a text to a list,
     * which doesn't happen that much for base64.
     * So this method should be smaller than faster.
     */
    public CharSequence subSequence(int start, int end) {
        StringBuilder buf = new StringBuilder();
        get();  // fill in the buffer if we haven't done so
        for( int i=start; i<end; i++ )
            buf.append(charAt(i));
        return buf;
    }

    public String toString() {
        return subSequence(0,length()).toString();
    }
}
