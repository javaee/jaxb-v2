package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.namespace.NamespaceContext;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Walks the XML document structure.
 *
 * Implemented by the unmarshaller and called by the API-specific connectors.
 *
 * <h2>Event Contracts</h2>
 * <ul>
 *  <li>Text events may not be called in a row.
 * </ul>
 *
 *
 * <h2>Connector's Responsibilities</h2>
 * <ul>
 *  <li>Maintains {@link NamespaceContext}.
 * </ul>
 *
 *
 *
 * <h2>Error Handling</h2>
 * The visitor may throw {@link SAXException} to abort the unmarshalling process
 * in the middle.
 *
 * @author Kohsuke Kawaguchi
 */
interface XmlVisitor {
    void startDocument(LocatorEx locator) throws SAXException;
    void endDocument() throws SAXException;

    /**
     * Notifies a start tag of a new element.
     *
     * namespace URIs and local names must be interned.
     */
    void startElement( String nsUri, String localName, String qname, Attributes atts ) throws SAXException;
    void endElement( String nsUri, String localName, String qname ) throws SAXException;

    /**
     * Called before {@link #startElement} event to notify a new namespace binding.
     */
    void startPrefixMapping( String prefix, String nsUri ) throws SAXException;
    /**
     * Called after {@link #endElement} event to notify the end of a binding.
     */
    void endPrefixMapping( String prefix ) throws SAXException;

    /**
     * Text events.
     *
     * @param pcdata
     *      represents characters.
     */
    void text( CharSequence pcdata ) throws SAXException;

    /**
     * Returns true if the visitor is expecting a text event as the next event.
     *
     * <p>
     * This is primarily intended to be used for optimization to avoid buffering
     * characters unnecessarily. If this method returns false and the connector
     * sees whitespace it can safely skip it.
     *
     * <p>
     * If this method returns true, all the whitespaces are considered significant
     * and thus need to be reported as a {@link #text} event. Furthermore,
     * if the element has no children (like &lt;foo/>), then it has to be reported
     * an empty {@link #text} event.
     */
    boolean expectText();

    /**
     * Returns the {@link UnmarshallingContext} at the end of the chain.
     */
    UnmarshallingContext getContext();
}
