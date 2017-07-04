/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.txw2.output;

import com.sun.xml.txw2.TxwException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.sax.SAXResult;
import java.util.Stack;

/**
 * {@link XmlSerializer} for {@link SAXResult} and {@link ContentHandler}.
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public class SaxSerializer implements XmlSerializer {

    private final ContentHandler writer;
    private final LexicalHandler lexical;

    public SaxSerializer(ContentHandler handler) {
        this(handler,null,true);
    }

    /**
     * Creates an {@link XmlSerializer} that writes SAX events.
     *
     * <p>
     * Sepcifying a non-null {@link LexicalHandler} allows applications
     * to write comments and CDATA sections.
     */
    public SaxSerializer(ContentHandler handler,LexicalHandler lex) {
        this(handler, lex, true);
    }

    public SaxSerializer(ContentHandler handler,LexicalHandler lex, boolean indenting) {
        if(!indenting) {
            writer = handler;
            lexical = lex;
        } else {
            IndentingXMLFilter indenter = new IndentingXMLFilter(handler, lex);
            writer = indenter;
            lexical = indenter;
        }
    }

    public SaxSerializer(SAXResult result) {
        this(result.getHandler(),result.getLexicalHandler());
    }


    // XmlSerializer implementation

    public void startDocument() {
        try {
            writer.startDocument();
        } catch (SAXException e) {
            throw new TxwException(e);
        }
    }

    // namespace prefix bindings
    // add in #writeXmlns and fired in #endStartTag
    private final Stack<String> prefixBindings = new Stack<String>();

    public void writeXmlns(String prefix, String uri) {
        // defend against parsers that pass null in for "xmlns" prefix
        if (prefix == null) {
            prefix = "";
        }

        if (prefix.equals("xml")) {
            return;
        }

        prefixBindings.add(uri);
        prefixBindings.add(prefix);
    }

    // element stack
    private final Stack<String> elementBindings = new Stack<String>();

    public void beginStartTag(String uri, String localName, String prefix) {
        // save element bindings for #endTag
        elementBindings.add(getQName(prefix, localName));
        elementBindings.add(localName);
        elementBindings.add(uri);
    }

    // attribute storage
    // attrs are buffered in #writeAttribute and sent to the content
    // handler in #endStartTag
    private final AttributesImpl attrs = new AttributesImpl();

    public void writeAttribute(String uri, String localName, String prefix, StringBuilder value) {
        attrs.addAttribute(uri,
                localName,
                getQName(prefix, localName),
                "CDATA",
                value.toString());
    }

    public void endStartTag(String uri, String localName, String prefix) {
        try {
            while (prefixBindings.size() != 0) {
                writer.startPrefixMapping(prefixBindings.pop(), // prefix
                        prefixBindings.pop()   // uri
                );
            }

            writer.startElement(uri,
                    localName,
                    getQName(prefix, localName),
                    attrs);

            attrs.clear();
        } catch (SAXException e) {
            throw new TxwException(e);
        }
    }

    public void endTag() {
        try {
            writer.endElement(elementBindings.pop(), // uri
                    elementBindings.pop(), // localName
                    elementBindings.pop()  // qname
            );
        } catch (SAXException e) {
            throw new TxwException(e);
        }
    }

    public void text(StringBuilder text) {
        try {
            writer.characters(text.toString().toCharArray(), 0, text.length());
        } catch (SAXException e) {
            throw new TxwException(e);
        }
    }

    public void cdata(StringBuilder text) {
        if(lexical==null)
            throw new UnsupportedOperationException("LexicalHandler is needed to write PCDATA");

        try {
            lexical.startCDATA();
            text(text);
            lexical.endCDATA();
        } catch (SAXException e) {
            throw new TxwException(e);
        }
    }

    public void comment(StringBuilder comment) {
        try {
            if(lexical==null)
                throw new UnsupportedOperationException("LexicalHandler is needed to write comments");
            else
                lexical.comment(comment.toString().toCharArray(), 0, comment.length() );
        } catch (SAXException e) {
            throw new TxwException(e);
        }
    }

    public void endDocument() {
        try {
            writer.endDocument();
        } catch (SAXException e) {
            throw new TxwException(e);
        }
    }

    public void flush() {
        // noop
    }

    // other methods
    private static String getQName(String prefix, String localName) {
        final String qName;
        if (prefix == null || prefix.length() == 0)
            qName = localName;
        else
            qName = prefix + ':' + localName;

        return qName;
    }
}
