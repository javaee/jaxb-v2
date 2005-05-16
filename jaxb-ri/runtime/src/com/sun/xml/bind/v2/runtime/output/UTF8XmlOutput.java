package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

import org.xml.sax.SAXException;

/**
 * {@link XmlOutput} implementation specialized for UTF-8.
 *
 * @author Kohsuke Kawaguchi
 */
public class UTF8XmlOutput extends XmlOutput {
    protected final OutputStream out;

    /** prefixes encoded. */
    private Encoded[] prefixes = new Encoded[8];

    /** local names encoded in UTF-8. All entries are pre-filled. */
    private final Encoded[] localNames;

    /** Temporary buffer used to encode text. */
    private final Encoded textBuffer = new Encoded();

    /**
     *
     * @param localNames
     *      local names encoded in UTF-8.
     */
    public UTF8XmlOutput(OutputStream out, Encoded[] localNames) {
        this.out = out;
        this.localNames = localNames;
        for( int i=0; i<prefixes.length; i++ )
            prefixes[i] = new Encoded();
        // TODO: check
        prefixes[0].set("xml:");
    }

    @Override
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment,nsUriIndex2prefixIndex,nsContext);
        if(!fragment) {
            out.write(XML_DECL);
        }
    }

    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        out.write('\n');
        super.endDocument(fragment);
    }

    public void beginStartTag(int prefix, String localName) throws IOException {
        int base= pushNsDecls();
        out.write('<');
        writeName(prefix,localName);
        writeNsDecls(base);
    }

    public void beginStartTag(Name name) throws IOException {
        int base = pushNsDecls();
        out.write('<');
        writeName(name);
        writeNsDecls(base);
    }

    private int pushNsDecls() {
        int total = nsContext.count();
        NamespaceContextImpl.Element ns = nsContext.getCurrent();

        if(total > prefixes.length) {
            // reallocate
            int m = Math.max(total,prefixes.length*2);
            Encoded[] buf = new Encoded[m];
            System.arraycopy(prefixes,0,buf,0,prefixes.length);
            for( int i=prefixes.length; i<buf.length; i++ )
                buf[i] = new Encoded();
            prefixes = buf;
        }

        int count = ns.count();
        int base = ns.getBase();
        for( int i=0; i<count; i++ ) {
            String p = ns.getPrefix(i);

            Encoded e = prefixes[base+i];

            if(p.length()==0) {
                e.buf = EMPTY_BYTE_ARRAY;
                e.len = 0;
            } else {
                e.set(p);
                e.append(':');
            }
        }
        return base;
    }

    private void writeNsDecls(int base) throws IOException {
        NamespaceContextImpl.Element ns = nsContext.getCurrent();
        int count = ns.count();

        for( int i=0; i<count; i++ ) {
            String p = ns.getPrefix(i);

            if(p.length()==0) {
                if(base==1 && ns.getNsUri(i).length()==0)
                    continue;   // no point in declaring xmlns="" on the root element
                out.write(XMLNS_EQUALS);
            } else {
                Encoded e = prefixes[base+i];
                out.write(XMLNS_COLON);
                out.write(e.buf,0,e.len-1); // skip the trailing ':'
                out.write(EQUALS);
            }
            doText(ns.getNsUri(i),true);
            out.write('\"');
        }
    }

    private void writePrefix(int prefix) throws IOException {
        Encoded e = prefixes[prefix];
        out.write(e.buf,0,e.len);
    }

    private void writeName(Name name) throws IOException {
        writePrefix(nsUriIndex2prefixIndex[name.nsUriIndex]);
        localNames[name.localNameIndex].write(out);
    }

    private void writeName(int prefix, String localName) throws IOException {
        writePrefix(prefix);
        textBuffer.set(localName);
        textBuffer.write(out);
    }

    @Override
    public void attribute(Name name, String value) throws IOException {
        out.write(' ');
        if(name.nsUriIndex==-1) {
            localNames[name.localNameIndex].write(out);
        } else
            writeName(name);
        out.write(EQUALS);
        doText(value,true);
        out.write('\"');
    }

    public void attribute(int prefix, String localName, String value) throws IOException {
        out.write(' ');
        if(prefix==-1) {
            textBuffer.set(localName);
            textBuffer.write(out);
        } else
            writeName(prefix,localName);
        out.write(EQUALS);
        doText(value,true);
        out.write('\"');
    }

    @Override
    public void attribute(Name name, char[] buf, int len, boolean needEscape) throws IOException {
        out.write(' ');
        if(name.nsUriIndex==-1) {
            textBuffer.set(name.localName);
            textBuffer.write(out);
        } else
            writeName(name);
        out.write(EQUALS);
        textBuffer.setEscape(buf,len,true);
        textBuffer.write(out);
        out.write('\"');
    }

    public void endStartTag() throws IOException {
        out.write('>');
    }

    @Override
    public void endTag(Name name) throws IOException {
        out.write(CLOSE_TAG);
        writeName(name);
        out.write('>');
    }

    public void endTag(int prefix, String localName) throws IOException {
        out.write(CLOSE_TAG);
        writeName(prefix,localName);
        out.write('>');
    }

    public void text(CharSequence value, boolean needSP) throws IOException {
        if(needSP)
            out.write(' ');
        doText(value,false);
    }

    private void doText(CharSequence value,boolean isAttribute) throws IOException {
        textBuffer.setEscape(value,isAttribute);
        textBuffer.write(out);
    }

    public void text(char[] buf, int len) throws IOException {
        textBuffer.setEscape(buf,len,false);
        textBuffer.write(out);
    }

    @Override
    public void text(int value) throws IOException, SAXException, XMLStreamException {
        if(value==0) {
            out.write((byte)'0');
        } else {
            // max is -2147483648 and 11 digits
            boolean minus = (value<0);
            textBuffer.ensureSize(11);
            byte[] buf = textBuffer.buf;
            int idx = 11;

            do {
                int r = value%10;
                if(r<0) r = -r;
                buf[--idx] = (byte)('0'|r);    // really measn 0x30+r but 0<=r<10, so bit-OR would do.
                value /= 10;
            } while(value!=0);

            if(minus)   buf[--idx] = (byte)'-';

            out.write(buf,idx,11-idx);
        }
    }

    public void flush() throws IOException {
        out.flush();
    }


    static final byte[] toBytes(String s) {
        byte[] buf = new byte[s.length()];
        for( int i=s.length()-1; i>=0; i-- )
            buf[i] = (byte)s.charAt(i);
        return buf;
    }

    private static final byte[] XMLNS_EQUALS = toBytes(" xmlns=\"");
    private static final byte[] XMLNS_COLON = toBytes(" xmlns:");
    private static final byte[] EQUALS = toBytes("=\"");
    private static final byte[] CLOSE_TAG = toBytes("</");
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] XML_DECL = toBytes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
}
