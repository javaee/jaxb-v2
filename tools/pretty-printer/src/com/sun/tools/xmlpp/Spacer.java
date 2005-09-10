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
