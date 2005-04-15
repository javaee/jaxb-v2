/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xmlpp;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Introduces additional NLs after cetrain end tags to enhance
 * readability.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class Spacer extends XMLFilterImpl {
    
    private final Set spacedTags = new HashSet();
    
    Spacer( XMLReader parent ) {
        this();
        setParent(parent);
    }
    
    Spacer() {
        // TODO: move this build.xml-specific logic to outside this class.
        spacedTags.add("target");
        spacedTags.add("taskdef");
    }

    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        super.endElement(uri, localName, qName);
        if( spacedTags.contains(localName) )
            characters("\n".toCharArray(),0,1);
        
    }

}
