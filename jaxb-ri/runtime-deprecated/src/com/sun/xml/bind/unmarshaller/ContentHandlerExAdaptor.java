/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.bind.unmarshaller;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Redirects events to another SAX ContentHandler.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since JAXB1.0
 * @deprecated in JAXB1.0.1
 */
public class ContentHandlerExAdaptor extends ContentHandlerEx {
    
    /**
     * This handler will receive SAX events.
     */
    private final ContentHandler handler;

    public ContentHandlerExAdaptor(UnmarshallingContext _ctxt,ContentHandler _handler) {
        // this class don't use the state, but to make the base class happy,
        // we need to pretend that we are always in the state #0.
        super(_ctxt, "-");
        this.handler = _handler;
        
        // emulate the start of documents
        try {
            handler.setDocumentLocator(context.getLocator());
            handler.startDocument();
        } catch( SAXException e ) {
            error(e);
        }
    }

    protected UnmarshallableObject owner() {
        return null;
    }


    // nest level of elements.
    private int depth = 0;
        
    public void enterAttribute(String uri, String local) throws UnreportedException {
    }

    public void enterElement(String uri, String local, Attributes atts) throws UnreportedException {
        depth++;
        // TODO: qname
        try {
            handler.startElement(uri,local,null,atts);
        } catch( SAXException e ) {
            error(e);
        }
    }

    public void leaveAttribute(String uri, String local) throws UnreportedException {
    }

    public void leaveElement(String uri, String local) throws UnreportedException {
        try {
            handler.endElement(uri,local,null); // TODO:qname
        } catch( SAXException e ) {
            error(e);
        }
        
        depth--;
        if(depth==0) {
            // emulate the end of the document
            try {
                handler.endDocument();
            } catch( SAXException e ) {
                error(e);
            }
            context.popContentHandler();
        }
    }

    public void text(String s) throws UnreportedException {
        try {
            handler.characters(s.toCharArray(),0,s.length());
        } catch( SAXException e ) {
            error(e);
        }
    }
    
    private void error( SAXException e ) {
        context.handleEvent(new ValidationEventImpl(
            ValidationEvent.ERROR,
            e.getMessage(),
            new ValidationEventLocatorImpl(context.getLocator()),
            e
        ));
    }
}
