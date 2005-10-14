package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import com.sun.xml.bind.WhiteSpaceProcessor;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Receives SAX events and convert them to our internal events.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SAXConnector implements UnmarshallerHandler {

    private LocatorEx loc;

    /**
     * SAX may fire consective characters event, but we don't allow it.
     * so use this buffer to perform buffering.
     */
    private final StringBuilder buffer = new StringBuilder();


    private final XmlVisitor next;
    private final UnmarshallingContext context;

    private static final class TagNameImpl extends TagName {
        String qname;
        public String getQname() {
            return qname;
        }
    }
    private final TagNameImpl tagName = new TagNameImpl();


    /**
     *
     * @param externalLocator
     *      If the caller is producing SAX events from sources other than Unicode and angle brackets,
     *      the caller can override the default SAX {@link Locator} object by this object
     *      to provide better location information.
     */
    public SAXConnector(XmlVisitor next, LocatorEx externalLocator ) {
        this.next = next;
        this.context = next.getContext();
        this.loc = externalLocator;
    }

    public Object getResult() throws JAXBException, IllegalStateException {
        return context.getResult();
    }

    public UnmarshallingContext getContext() {
        return context;
    }

    public void setDocumentLocator(final Locator locator) {
        if(loc!=null)
            return; // we already have an external locator. ignore.

        this.loc = new LocatorEx() {
            public ValidationEventLocator getLocation() {
                return new ValidationEventLocatorImpl(locator);
            }
            public String getPublicId() {
                return locator.getPublicId();
            }
            public String getSystemId() {
                return locator.getSystemId();
            }
            public int getLineNumber() {
                return locator.getLineNumber();
            }
            public int getColumnNumber() {
                return locator.getColumnNumber();
            }
        };
    }

    public void startDocument() throws SAXException {
        next.startDocument(loc,null);
    }

    public void endDocument() throws SAXException {
        next.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        next.startPrefixMapping(prefix,uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        next.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        // work gracefully with misconfigured parsers that don't support namespaces
        if( uri==null || uri.length()==0 )
            uri="";
        if( local==null || local.length()==0 )
            local=qname;
        if( qname==null || qname.length()==0 )
            qname=local;

        processText(true);

        tagName.uri = uri;
        tagName.local = local;
        tagName.qname = qname;
        tagName.atts = atts;
        next.startElement(tagName);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processText(false);
        tagName.uri = uri;
        tagName.local = localName;
        tagName.qname = qName;
        next.endElement(tagName);
    }


    public final void characters( char[] buf, int start, int len ) {
        if( context.expectText() )
            buffer.append(buf,start,len);
    }

    public final void ignorableWhitespace( char[] buf, int start, int len ) {
        characters(buf,start,len);
    }

    public void processingInstruction(String target, String data) {
        // nop
    }

    public void skippedEntity(String name) {
        // nop
    }

    private void processText( boolean ignorable ) throws SAXException {
        if( context.expectText() && (!ignorable || !WhiteSpaceProcessor.isWhiteSpace(buffer)))
            next.text(buffer);
        buffer.setLength(0);
    }
}
