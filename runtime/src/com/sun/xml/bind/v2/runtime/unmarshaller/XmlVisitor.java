package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Walks the XML document structure.
 *
 * Implemented by the unmarshaller and called by the API-specific connectors.
 *
 * <h2>Event Call Sequence</h2>
 *
 * The {@link XmlVisitor} expects the event callbacks in the following order:
 * <pre>
 * CALL SEQUENCE := startDocument ELEMENT endDocument
 * ELEMENT       := startPrefixMapping ELEMENT endPrefixMapping
 *               |  startElement BODY endElement
 * BODY          := text? (ELEMENT text?)*
 * </pre>
 * Note in particular that text events may not be called in a row;
 * consecutive characters (even those separated by PIs and comments)
 * must be reported as one event, unlike SAX.
 *
 * <p>
 * All namespace URIs, local names, and prefixes of element and attribute
 * names must be interned. qnames need not be interned.
 *
 *
 * <h2>Typed PCDATA</h2>
 * For efficiency, JAXB RI defines a few {@link CharSequence} implementations
 * that can be used as a parameter to the {@link #text(CharSequence)} method.
 * For example, see {@link Base64Data}.
 *
 * <h2>Error Handling</h2>
 * The visitor may throw {@link SAXException} to abort the unmarshalling process
 * in the middle.
 *
 * @author Kohsuke Kawaguchi
 */
public interface XmlVisitor {
    void startDocument(LocatorEx locator) throws SAXException;
    void endDocument() throws SAXException;

    /**
     * Notifies a start tag of a new element.
     *
     * namespace URIs and local names must be interned.
     *
     * @param atts
     *      the attributes of this element. must not be null.
     *      need not be immutable; the caller may reuse the same
     *      {@link Attributes} object for multiple elements.
     *
     *      Implementataions are carefully done to allow {@link AttributesEx} to be passed
     *      in this parameter, allowing efficient typed attribute values to be used for the unmarshalling. 
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
     * <p>
     * The caller should consult <tt>getContext().expectText()</tt> to see
     * if the unmarshaller is expecting any PCDATA. If the above is returning
     * false, the caller is OK to skip any text in XML. The net effect is
     * that we can ignore whitespaces quickly.
     *
     * @param pcdata
     *      represents character data. This object can be mutable
     *      (such as {@link StringBuilder}); it only needs to be fixed
     *      while this method is executing.
     */
    void text( CharSequence pcdata ) throws SAXException;

    /**
     * Returns the {@link UnmarshallingContext} at the end of the chain.
     *
     * @return
     *      always return the same object, so caching the result is recommended.
     */
    UnmarshallingContext getContext();
}
