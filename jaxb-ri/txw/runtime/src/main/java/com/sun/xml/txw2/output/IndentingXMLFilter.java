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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Stack;

/**
 * {@link XMLFilterImpl} that does indentation to SAX events.
 *
 * @author Kohsuke Kawaguchi
 */
public class IndentingXMLFilter extends XMLFilterImpl implements LexicalHandler {
    private LexicalHandler lexical;

    public IndentingXMLFilter() {
    }

    public IndentingXMLFilter(ContentHandler handler) {
        setContentHandler(handler);
    }

    public IndentingXMLFilter(ContentHandler handler, LexicalHandler lexical) {
        setContentHandler(handler);
        setLexicalHandler(lexical);
    }

    public LexicalHandler getLexicalHandler() {
        return lexical;
    }

    public void setLexicalHandler(LexicalHandler lexical) {
        this.lexical = lexical;
    }


    /**
     * Return the current indent step.
     *
     * <p>Return the current indent step: each start tag will be
     * indented by this number of spaces times the number of
     * ancestors that the element has.</p>
     *
     * @return The number of spaces in each indentation step,
     *         or 0 or less for no indentation.
     * @see #setIndentStep(int)
     *
     * @deprecated
     *      Only return the length of the indent string.
     */
    public int getIndentStep ()
    {
        return indentStep.length();
    }


    /**
     * Set the current indent step.
     *
     * @param indentStep The new indent step (0 or less for no
     *        indentation).
     * @see #getIndentStep()
     *
     * @deprecated
     *      Should use the version that takes string.
     */
    public void setIndentStep (int indentStep)
    {
        StringBuilder s = new StringBuilder();
        for( ; indentStep>0; indentStep-- )   s.append(' ');
        setIndentStep(s.toString());
    }

    public void setIndentStep(String s) {
        this.indentStep = s;
    }



    ////////////////////////////////////////////////////////////////////
    // Override methods from XMLWriter.
    ////////////////////////////////////////////////////////////////////

    /**
     * Write a start tag.
     *
     * <p>Each tag will begin on a new line, and will be
     * indented by the current indent step times the number
     * of ancestors that the element has.</p>
     *
     * <p>The newline and indentation will be passed on down
     * the filter chain through regular characters events.</p>
     *
     * @param uri The element's Namespace URI.
     * @param localName The element's local name.
     * @param qName The element's qualified (prefixed) name.
     * @param atts The element's attribute list.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the start tag, or if a filter further
     *            down the chain raises an exception.
     * @see XMLWriter#startElement(String, String, String,Attributes)
     */
    public void startElement (String uri, String localName,
                              String qName, Attributes atts)
        throws SAXException {
        stateStack.push(SEEN_ELEMENT);
        state = SEEN_NOTHING;
        if (depth > 0) {
            writeNewLine();
        }
        doIndent();
        super.startElement(uri, localName, qName, atts);
        depth++;
    }

    private void writeNewLine() throws SAXException {
        super.characters(NEWLINE,0,NEWLINE.length);
    }

    private static final char[] NEWLINE = {'\n'};


    /**
     * Write an end tag.
     *
     * <p>If the element has contained other elements, the tag
     * will appear indented on a new line; otherwise, it will
     * appear immediately following whatever came before.</p>
     *
     * <p>The newline and indentation will be passed on down
     * the filter chain through regular characters events.</p>
     *
     * @param uri The element's Namespace URI.
     * @param localName The element's local name.
     * @param qName The element's qualified (prefixed) name.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the end tag, or if a filter further
     *            down the chain raises an exception.
     * @see XMLWriter#endElement(String, String, String)
     */
    public void endElement (String uri, String localName, String qName)
        throws SAXException
    {
        depth--;
        if (state == SEEN_ELEMENT) {
            writeNewLine();
            doIndent();
        }
        super.endElement(uri, localName, qName);
        state = stateStack.pop();
    }


//    /**
//     * Write a empty element tag.
//     *
//     * <p>Each tag will appear on a new line, and will be
//     * indented by the current indent step times the number
//     * of ancestors that the element has.</p>
//     *
//     * <p>The newline and indentation will be passed on down
//     * the filter chain through regular characters events.</p>
//     *
//     * @param uri The element's Namespace URI.
//     * @param localName The element's local name.
//     * @param qName The element's qualified (prefixed) name.
//     * @param atts The element's attribute list.
//     * @exception org.xml.sax.SAXException If there is an error
//     *            writing the empty tag, or if a filter further
//     *            down the chain raises an exception.
//     * @see XMLWriter#emptyElement(String, String, String, Attributes)
//     */
//    public void emptyElement (String uri, String localName,
//                              String qName, Attributes atts)
//        throws SAXException
//    {
//        state = SEEN_ELEMENT;
//        if (depth > 0) {
//            super.characters("\n");
//        }
//        doIndent();
//        super.emptyElement(uri, localName, qName, atts);
//    }


    /**
     * Write a sequence of characters.
     *
     * @param ch The characters to write.
     * @param start The starting position in the array.
     * @param length The number of characters to use.
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the characters, or if a filter further
     *            down the chain raises an exception.
     * @see XMLWriter#characters(char[], int, int)
     */
    public void characters (char ch[], int start, int length)
        throws SAXException
    {
        state = SEEN_DATA;
        super.characters(ch, start, length);
    }

    public void comment(char ch[], int start, int length) throws SAXException {
        if (depth > 0) {
            writeNewLine();
        }
        doIndent();
        if(lexical!=null)
            lexical.comment(ch,start,length);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        if(lexical!=null)
            lexical.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
        if(lexical!=null)
            lexical.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        if(lexical!=null)
            lexical.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        if(lexical!=null)
            lexical.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        if(lexical!=null)
            lexical.startCDATA();
    }

    public void endCDATA() throws SAXException {
        if(lexical!=null)
            lexical.endCDATA();
    }

    ////////////////////////////////////////////////////////////////////
    // Internal methods.
    ////////////////////////////////////////////////////////////////////


    /**
     * Print indentation for the current level.
     *
     * @exception org.xml.sax.SAXException If there is an error
     *            writing the indentation characters, or if a filter
     *            further down the chain raises an exception.
     */
    private void doIndent ()
        throws SAXException
    {
        if (depth > 0) {
            char[] ch = indentStep.toCharArray();
            for( int i=0; i<depth; i++ )
                characters(ch, 0, ch.length);
        }
    }


    ////////////////////////////////////////////////////////////////////
    // Constants.
    ////////////////////////////////////////////////////////////////////

    private final static Object SEEN_NOTHING = new Object();
    private final static Object SEEN_ELEMENT = new Object();
    private final static Object SEEN_DATA = new Object();


    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    private Object state = SEEN_NOTHING;
    private Stack<Object> stateStack = new Stack<Object>();

    private String indentStep = "";
    private int depth = 0;
}
