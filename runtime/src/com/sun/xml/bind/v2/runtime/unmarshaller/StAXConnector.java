package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.namespace.NamespaceContext;

/**
 *
 * <p>
 * Implements {@link NamespaceContext} because the StAX API is unclear about
 * whether the context returned from a StAX parser is live or not.
 *
 * @author Kohsuke Kawaguchi
 */
public class StAXConnector {} // TODO
//public class StAXConnector implements NamespaceContext {
//
//    private XmlVisitor next;
//
//    private final XMLStreamReader reader;
//
//    public StAXConnector(XMLStreamReader reader, XmlVisitor next) {
//        this.reader = reader;
//        this.next = next;
//    }
//
//    public void connect() throws XMLStreamException {
//        // remembers the nest level of elements to know when we are done.
//        int depth=0;
//
//        // if the parser is at the start tag, proceed to the first element
//        int event = reader.getEventType();
//        if(event == XMLStreamConstants.START_DOCUMENT) {
//            // nextTag doesn't correctly handle DTDs
//            while( !reader.isStartElement() )
//                event = reader.next();
//        }
//
//
//        if( event!=XMLStreamConstants.START_ELEMENT)
//            throw new IllegalStateException("The current event is not START_ELEMENT\n but " + event);
//
//        next.startDocument(null/*TODO*/,this);
//
//        do {
//            // These are all of the events listed in the javadoc for
//            // XMLEvent.
//            // The spec only really describes 11 of them.
//            switch (event) {
//                case XMLStreamConstants.START_ELEMENT :
//                    depth++;
//                    handleStartElement();
//                    break;
//                case XMLStreamConstants.END_ELEMENT :
//                    handleEndElement();
//                    depth--;
//                    break;
//                case XMLStreamConstants.CDATA :
//                case XMLStreamConstants.SPACE :
//                case XMLStreamConstants.CHARACTERS :
//                    handleCharacters();
//                    break;
//                case XMLStreamConstants.ATTRIBUTE :
//                    handleAttribute();
//                    break;
//                case XMLStreamConstants.NAMESPACE :
//                    handleNamespace();
//                    break;
//                default :
//                    break;  // just ignore
//            }
//
//            event=reader.next();
//        } while (depth!=0);
//
//        next.endDocument();
//    }
//
//    private void handleCharacters() {
//        // no-op ???
//        // this event is listed in the javadoc, but not in the spec.
//    }
//
//
////
////
//// NamespaceContext implementations
////
////
//    public String getNamespaceURI(String prefix) {
//        return reader.getNamespaceContext().getNamespaceURI(prefix);
//    }
//
//    public String getPrefix(String namespaceURI) {
//        return reader.getNamespaceContext().getPrefix(namespaceURI);
//    }
//
//    public Iterator getPrefixes(String namespaceURI) {
//        return reader.getNamespaceContext().getPrefixes(namespaceURI);
//    }
//}
