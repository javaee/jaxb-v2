/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;

import com.sun.xml.bind.v2.FatalAdapter;

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
        // if the user bothers to use a validator, make validation errors fatal
        // so that it will abort unmarshalling.
        validator.setErrorHandler(new FatalAdapter(getContext()));
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

    public void startElement(TagName tagName) throws SAXException {
        validator.startElement(tagName.uri,tagName.local,tagName.getQname(),tagName.atts);
        next.startElement(tagName);
    }

    public void endElement(TagName tagName ) throws SAXException {
        validator.endElement(tagName.uri,tagName.local,tagName.getQname());
        next.endElement(tagName);
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

    public UnmarshallingContext getContext() {
        return next.getContext();
    }
}
