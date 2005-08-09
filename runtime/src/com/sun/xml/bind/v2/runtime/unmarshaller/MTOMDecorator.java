package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import com.sun.xml.bind.v2.WellKnownNamespace;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Decorator of {@link XmlVisitor} that performs XOP processing.
 * Used to support MTOM.
 *
 * @author Kohsuke Kawaguchi
 */
final class MTOMDecorator implements XmlVisitor {

    private final XmlVisitor next;

    private final AttachmentUnmarshaller au;

    private UnmarshallerImpl parent;

    private final Base64Data base64data = new Base64Data();

    /**
     * True if we are between the start and the end of xop:Include
     */
    private boolean inXopInclude;

    /**
     * UGLY HACK: we need to ignore the whitespace that follows
     * the attached base64 image.
     *
     * This happens twice; once before &lt;/xop:Include>, another
     * after &lt;/xop:Include>. The spec guarantees that
     * no valid pcdata can follow &lt;/xop:Include>. 
     */
    private boolean followXop;

    public MTOMDecorator(UnmarshallerImpl parent,XmlVisitor next, AttachmentUnmarshaller au) {
        this.parent = parent;
        this.next = next;
        this.au = au;
    }

    public void startDocument(LocatorEx loc) throws SAXException {
        next.startDocument(loc);
    }

    public void endDocument() throws SAXException {
        next.endDocument();
    }

    public void startElement(TagName tagName) throws SAXException {
        if(tagName.local=="Include" && tagName.uri==WellKnownNamespace.XOP) {
            // found xop:Include
            String href = tagName.atts.getValue("href");
            DataHandler attachment = au.getAttachmentAsDataHandler(href);
            if(attachment==null) {
                // report an error and ignore
                parent.getEventHandler().handleEvent(null);
                // TODO
            }
            base64data.set(attachment);
            next.text(base64data);
            inXopInclude = true;
            followXop = true;
        } else
            next.startElement(tagName);
    }

    public void endElement(TagName tagName) throws SAXException {
        if(inXopInclude) {
            // consume </xop:Include> by ourselves.
            inXopInclude = false;
            followXop = true;
            return;
        }
        next.endElement(tagName);
    }

    public void startPrefixMapping(String prefix, String nsUri) throws SAXException {
        next.startPrefixMapping(prefix,nsUri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        next.endPrefixMapping(prefix);
    }

    public void text( CharSequence pcdata ) throws SAXException {
        if(!followXop)
            next.text(pcdata);
        else
            followXop = false;
    }

    public UnmarshallingContext getContext() {
        return next.getContext();
    }
}
