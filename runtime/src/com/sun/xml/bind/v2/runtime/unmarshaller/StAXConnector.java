package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class StAXConnector {
    public abstract void bridge() throws XMLStreamException;


    // event sink
    protected final XmlVisitor visitor;

    protected final UnmarshallingContext context;

    private final class TagNameImpl extends TagName {
        public String getQname() {
            return StAXConnector.this.getCurrentQName();
        }
    }

    protected final TagName tagName = new TagNameImpl();

    protected StAXConnector(XmlVisitor visitor) {
        this.visitor = visitor;
        context = visitor.getContext();
    }

    /**
     * Gets the {@link Location}. Used for implementing the line number information.
     * @return must not null.
     */
    protected abstract Location getCurrentLocation();

    /**
     * Gets the QName of the current element.
     */
    protected abstract String getCurrentQName();

    protected final void handleStartDocument() throws SAXException {
        visitor.startDocument(new LocatorEx() {
            public ValidationEventLocator getLocation() {
                return new ValidationEventLocatorImpl(this);
            }
            public int getColumnNumber() {
                return getCurrentLocation().getColumnNumber();
            }
            public int getLineNumber() {
                return getCurrentLocation().getLineNumber();
            }
            public String getPublicId() {
                return getCurrentLocation().getPublicId();
            }
            public String getSystemId() {
                return getCurrentLocation().getSystemId();
            }
        });
    }

    protected final void handleEndDocument() throws SAXException {
        visitor.endDocument();
    }

    protected static String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }

    protected final String getQName(String prefix, String localName) {
        if(prefix==null)
            return localName;
        else
            return prefix + ':' + localName;
    }
}
