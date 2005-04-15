/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * {@link XmlVisitor} decorator that validates the events by using JAXP validation API.
 *
 * @author Kohsuke Kawaguchi
 */
final class ValidatingUnmarshaller implements XmlVisitor {
    
    private final XmlVisitor next;
    private final ValidatorHandler validator;

    private char[] buf = new char[256];

    /**
     * Creates a new instance of ValidatingUnmarshaller.
     */
    public ValidatingUnmarshaller( Schema schema, XmlVisitor next ) {
        this.validator = schema.newValidatorHandler();
        this.next = next;
        validator.setErrorHandler(getContext());
    }

    public void startDocument(LocatorEx locator) throws SAXException {
        validator.setDocumentLocator(locator);
        validator.startDocument();
        next.startDocument(locator);
    }

    public void endDocument() throws SAXException {
        validator.endDocument();
        next.endDocument();
    }

    public void startElement( String nsUri, String localName, String qname, Attributes atts ) throws SAXException {
        validator.startElement(nsUri,localName,qname,atts);
        next.startElement(nsUri, localName, qname, atts);
    }

    public void endElement( String nsUri, String localName, String qname ) throws SAXException {
        validator.endElement(nsUri,localName,qname);
        next.endElement(nsUri, localName, qname);
    }

    public void startPrefixMapping(String prefix, String nsUri) throws SAXException {
        validator.startPrefixMapping(prefix,nsUri);
        next.startPrefixMapping(prefix,nsUri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        validator.endPrefixMapping(prefix);
        next.endPrefixMapping(prefix);
    }

    public void text( CharSequence pcdata ) throws SAXException {
        int len = pcdata.length();
        if(buf.length<len) {
            buf = new char[len];
        }
        for( int i=0;i<len; i++ )
            buf[i] = pcdata.charAt(i);  // isn't this kinda slow?

        validator.characters(buf,0,len);
        next.text(pcdata);
    }

    public boolean expectText() {
        return next.expectText();
    }

    public UnmarshallingContext getContext() {
        return next.getContext();
    }
}
