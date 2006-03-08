package com.sun.tools.xjc.reader.internalizer;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * {@link XMLReader} filter for supporting
 * <tt>http://xml.org/sax/features/namespace-prefixes</tt> feature.
 *
 * @author Kohsuke Kawaguchi
 */
final class ContentHandlerNamespacePrefixAdapter extends XMLFilterImpl {
    /**
     * True if <tt>http://xml.org/sax/features/namespace-prefixes</tt> is set to true.
     */
    private boolean namespacePrefixes = false;

    private String[] nsBinding = new String[8];
    private int len;

    public ContentHandlerNamespacePrefixAdapter() {
    }

    public ContentHandlerNamespacePrefixAdapter(XMLReader parent) {
        setParent(parent);
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if(name.equals(FEATURE))
            return namespacePrefixes;
        return super.getFeature(name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if(name.equals(FEATURE)) {
            this.namespacePrefixes = value;
            return;
        }
        super.setFeature(name, value);
    }


    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if(len==nsBinding.length) {
            // reallocate
            String[] buf = new String[nsBinding.length*2];
            System.arraycopy(nsBinding,0,buf,0,nsBinding.length);
            nsBinding = buf;
        }
        nsBinding[len++] = prefix;
        nsBinding[len++] = uri;
        super.startPrefixMapping(prefix,uri);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if(namespacePrefixes) {
            this.atts.setAttributes(atts);
            // add namespace bindings back as attributes
            for( int i=0; i<len; i+=2 ) {
                String prefix = nsBinding[i];
                if(prefix.length()==0)
                    this.atts.addAttribute(XMLConstants.XML_NS_URI,"xmlns","xmlns","CDATA",nsBinding[i+1]);
                else
                    this.atts.addAttribute(XMLConstants.XML_NS_URI,prefix,"xmlns:"+prefix,"CDATA",nsBinding[i+1]);
            }
            atts = this.atts;
        }
        len=0;
        super.startElement(uri, localName, qName, atts);
    }

    private final AttributesImpl atts = new AttributesImpl();

    private static final String FEATURE = "http://xml.org/sax/features/namespace-prefixes";
}
