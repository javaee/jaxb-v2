/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.bind.v2.runtime;

import com.sun.istack.FinalArrayList;
import com.sun.istack.SAXException2;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Receives SAX2 events and send the equivalent events to
 * {@link XMLSerializer}
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ContentHandlerAdaptor extends DefaultHandler {

    /** Stores newly declared prefix-URI mapping. */
    private final FinalArrayList<String> prefixMap = new FinalArrayList<String>();

    /** Events will be sent to this object. */
    private final XMLSerializer serializer;
    
    private final StringBuffer text = new StringBuffer();
    
    
    ContentHandlerAdaptor( XMLSerializer _serializer ) {
        this.serializer = _serializer;
    }
    
    public void startDocument() {
        prefixMap.clear();
    }

    public void startPrefixMapping(String prefix, String uri) {
        prefixMap.add(prefix);
        prefixMap.add(uri);
    }

    private boolean containsPrefixMapping(String prefix, String uri) {
        for( int i=0; i<prefixMap.size(); i+=2 ) {
            if(prefixMap.get(i).equals(prefix)
            && prefixMap.get(i+1).equals(uri))
                return true;
        }
        return false;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        try {
            flushText();

            int len = atts.getLength();

            String p = getPrefix(qName);

            // is this prefix going to be declared on this element?
            if(containsPrefixMapping(p,namespaceURI))
                serializer.startElementForce(namespaceURI,localName,p,null);
            else
                serializer.startElement(namespaceURI,localName, p,null);

            // declare namespace events
            for (int i = 0; i < prefixMap.size(); i += 2) {
                // forcibly set this binding, instead of using declareNsUri.
                // this guarantees that namespaces used in DOM will show up
                // as-is in the marshalled output (instead of reassigned to something else,
                // which may happen if you'd use declareNsUri.)
                serializer.getNamespaceContext().force(
                        prefixMap.get(i + 1), prefixMap.get(i));
            }

            // make sure namespaces needed by attributes are bound
            for( int i=0; i<len; i++ ) {
                String qname = atts.getQName(i);
                if(qname.startsWith("xmlns") || atts.getURI(i).length() == 0)
                    continue;
                String prefix = getPrefix(qname);

                serializer.getNamespaceContext().declareNamespace(
                    atts.getURI(i), prefix, true );
            }

            serializer.endNamespaceDecls(null);
            // fire attribute events
            for( int i=0; i<len; i++ ) {
                // be defensive.
                if(atts.getQName(i).startsWith("xmlns"))
                    continue;
                serializer.attribute( atts.getURI(i), atts.getLocalName(i), atts.getValue(i));
            }
            prefixMap.clear();
            serializer.endAttributes();
        } catch (IOException e) {
            throw new SAXException2(e);
        } catch (XMLStreamException e) {
            throw new SAXException2(e);
        }
    }

    private String getPrefix(String qname) {
        int idx = qname.indexOf(':');
        String prefix = (idx == -1) ? "" : qname.substring(0, idx);
        return prefix;
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        try {
            flushText();
            serializer.endElement();
        } catch (IOException e) {
            throw new SAXException2(e);
        } catch (XMLStreamException e) {
            throw new SAXException2(e);
        }
    }
    
    private void flushText() throws SAXException, IOException, XMLStreamException {
        if( text.length()!=0 ) {
            serializer.text(text.toString(),null);
            text.setLength(0);
        }
    }

    public void characters(char[] ch, int start, int length) {
        text.append(ch,start,length);
    }
}
