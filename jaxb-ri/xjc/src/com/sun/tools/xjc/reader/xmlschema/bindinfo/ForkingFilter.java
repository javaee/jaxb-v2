/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.xml.bind.v2.WellKnownNamespace;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * {@link XMLFilter} that can fork an event to another {@link ContentHandler}
 * in the middle.
 *
 * <p>
 * The side handler receives SAX events before the next handler in the filter chain does.
 *
 * @author Kohsuke Kawaguchi
 */
public class ForkingFilter extends XMLFilterImpl {

    /**
     * Non-null if we are also forking events to this handler.
     */
    private ContentHandler side;

    /**
     * The depth of the current element that the {@link #side} handler
     * is seeing.
     */
    private int depth;

    /**
     * In-scope namespace mapping.
     * namespaces[2n  ] := prefix
     * namespaces[2n+1] := namespace URI
     */
    private final ArrayList<String> namespaces = new ArrayList<String>();

    private Locator loc;

    public ForkingFilter() {
    }

    public ForkingFilter(ContentHandler next) {
        setContentHandler(next);
    }

    public ContentHandler getSideHandler() {
        return side;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.loc = locator;
    }

    public Locator getDocumentLocator() {
        return loc;
    }

    @Override
    public void startDocument() throws SAXException {
        reset();
        super.startDocument();
    }

    private void reset() {
        namespaces.clear();
        side = null;
        depth = 0;
    }

    @Override
    public void endDocument() throws SAXException {
        loc = null;
        reset();
        super.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (WellKnownNamespace.XML_NAMESPACE_URI.equals(uri)) return; //xml prefix shall not be declared based on jdk api javadoc
        if(side!=null)
            side.startPrefixMapping(prefix,uri);
        namespaces.add(prefix);
        namespaces.add(uri);
        super.startPrefixMapping(prefix,uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if ("xml".equals(prefix)) return; //xml prefix shall not be declared based on jdk api javadoc
        if(side!=null)
            side.endPrefixMapping(prefix);
        super.endPrefixMapping(prefix);
        namespaces.remove(namespaces.size()-1);
        namespaces.remove(namespaces.size()-1);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if(side!=null) {
            side.startElement(uri,localName,qName,atts);
            depth++;
        }
        super.startElement(uri, localName, qName, atts);
    }

    /**
     * Starts the event forking.
     */
    public void startForking(String uri, String localName, String qName, Attributes atts, ContentHandler side) throws SAXException {
        if(this.side!=null)     throw new IllegalStateException();  // can't fork to two handlers

        this.side = side;
        depth = 1;
        side.setDocumentLocator(loc);
        side.startDocument();
        for( int i=0; i<namespaces.size(); i+=2 )
            side.startPrefixMapping(namespaces.get(i),namespaces.get(i+1));
        side.startElement(uri,localName,qName,atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(side!=null) {
            side.endElement(uri,localName,qName);
            depth--;
            if(depth==0) {
                for( int i=namespaces.size()-2; i>=0; i-=2 )
                    side.endPrefixMapping(namespaces.get(i));
                side.endDocument();
                side = null;
            }
        }
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if(side!=null)
            side.characters(ch, start, length);
        super.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        if(side!=null)
            side.ignorableWhitespace(ch, start, length);
        super.ignorableWhitespace(ch, start, length);
    }
}
