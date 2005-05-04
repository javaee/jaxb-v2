/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.internalizer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * XMLFilter that finds references to other schema files from
 * SAX events.
 * 
 * This implementation is a base implementation for typical case
 * where we just need to look for a particular attribute which
 * contains an URL to another schema file.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class AbstractReferenceFinderImpl extends XMLFilterImpl {
    protected final DOMForest parent;
        
    protected AbstractReferenceFinderImpl( DOMForest _parent ) {
        this.parent = _parent;
    }
    
    /**
     * IF the given element contains a reference to an external resource,
     * return its URL.
     * 
     * @param nsURI
     *      Namespace URI of the current element
     * @param localName
     *      Local name of the current element
     * @return
     *      It's OK to return a relative URL.
     */
    protected abstract String findExternalResource( String nsURI, String localName, Attributes atts);
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        super.startElement(namespaceURI, localName, qName, atts);
        
        String relativeRef = findExternalResource(namespaceURI,localName,atts);
        if(relativeRef==null)   return; // non found
        
        try {
            // absolutize URL.
            String ref = new URI(locator.getSystemId()).resolve(new URI(relativeRef)).toString();

            // then parse this schema as well,
            // but don't mark this document as a root.
            parent.parse(ref,false);
        } catch( URISyntaxException e ) {
            SAXParseException spe = new SAXParseException(
                Messages.format(Messages.ERR_UNABLE_TO_PARSE,relativeRef,e.getMessage()),
                locator, e );
                
            fatalError(spe);
            throw spe;
        } catch( IOException e ) {
            SAXParseException spe = new SAXParseException(
                Messages.format(Messages.ERR_UNABLE_TO_PARSE,relativeRef,e.getMessage()),
                locator, e );

            fatalError(spe);
            throw spe;
        }
    }
        
    private Locator locator;
        
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }
};
