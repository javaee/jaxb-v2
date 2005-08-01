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
public class SAXConnector implements UnmarshallerHandler {

    private LocatorEx loc;

    /**
     * SAX may fire consective characters event, but we don't allow it.
     * so use this buffer to perform buffering.
     */
    private StringBuilder buffer = new StringBuilder();


    private final XmlVisitor next;


    /**
     *
     * @param externalLocator
     *      If the caller is producing SAX events from sources other than Unicode and angle brackets,
     *      the caller can override the default SAX {@link Locator} object by this object
     *      to provide better location information.
     */
    public SAXConnector(XmlVisitor next, LocatorEx externalLocator ) {
        this.next = next;
        this.loc = externalLocator;
    }

    public Object getResult() throws JAXBException, IllegalStateException {
        return next.getContext().getResult();
    }

    public UnmarshallingContext getContext() {
        return next.getContext();
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
        next.startDocument(loc);
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

        next.startElement(uri,local,qname,atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processText(false);
        next.endElement(uri,localName,qName);
    }


    public final void characters( char[] buf, int start, int len ) {
        if( next.expectText() )
            buffer.append(buf,start,len);
    }

    public final void ignorableWhitespace( char[] buf, int start, int len ) {
        characters(buf,start,len);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        ; // nop
    }

    public void skippedEntity(String name) throws SAXException {
        ; // nop
    }

    private void processText( boolean ignorable ) throws SAXException {
        if( next.expectText() && (!ignorable || !WhiteSpaceProcessor.isWhiteSpace(buffer)))
            next.text(buffer);

        // avoid excessive object allocation, but also avoid
        // keeping a huge array inside StringBuffer.
        if(buffer.length()<1024)    buffer.setLength(0);
        else                        buffer = new StringBuilder();
    }
}
