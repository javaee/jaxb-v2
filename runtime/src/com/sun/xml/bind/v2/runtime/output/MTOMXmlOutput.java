package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;

import org.xml.sax.SAXException;

/**
 * {@link XmlOutput} decorator that supports MTOM.
 *
 * @author Kohsuke Kawaguchi
 */
public final class MTOMXmlOutput extends XmlOutput {

    private final XmlOutput next;

    public MTOMXmlOutput(XmlOutput next) {
        this.next = next;
    }

    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer,fragment,nsUriIndex2prefixIndex, nsContext);
        next.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
    }

    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        next.endDocument(fragment);
        super.endDocument(fragment);
    }

    public void beginStartTag(Name name) throws IOException, XMLStreamException {
        next.beginStartTag(name);
    }

    public void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException {
        next.beginStartTag(prefix, localName);
    }

    public void attribute( Name name, String value ) throws IOException, XMLStreamException {
        next.attribute(name, value);
    }

    public void attribute( int prefix, String localName, String value ) throws IOException, XMLStreamException {
        next.attribute(prefix, localName, value);
    }

    public void attribute( Name name, char[] buf, int len, boolean needEscape ) throws IOException, XMLStreamException {
        next.attribute(name, buf, len, needEscape);
    }

    public void endStartTag() throws IOException, SAXException {
        next.endStartTag();
    }

    public void endTag(Name name) throws IOException, SAXException, XMLStreamException {
        next.endTag(name);
    }

    public void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException {
        next.endTag(prefix, localName);
    }

    public void text( CharSequence value, boolean needsSeparatingWhitespace ) throws IOException, SAXException, XMLStreamException {
        if(value instanceof Base64Data) {
            Base64Data b64d = (Base64Data) value;
            String cid = serializer.attachmentMarshaller.addMtomAttachment(b64d.getExact(),null,null);
            if(cid!=null) {
                nsContext.getCurrent().push();
                int prefix = nsContext.declareNsUri(WellKnownNamespace.XOP,"xop",false);
                beginStartTag(prefix,"Include");
                attribute(-1,"href",cid);
                endStartTag();
                endTag(prefix,"Include");
                nsContext.getCurrent().pop();
                return;
            }
        }
        next.text(value, needsSeparatingWhitespace);
    }

    public void text( char[] buf, int len ) throws IOException, SAXException, XMLStreamException {
        next.text(buf, len);
    }

    public void flush() throws IOException, XMLStreamException {
        next.flush();
    }
}
