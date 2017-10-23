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

//@@3RD PARTY CODE@@

// DataWriter.java - XML writer for data-oriented files.

package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Write data- or field-oriented XML.
 *
 * <p>This filter pretty-prints field-oriented XML without mixed content.
 * all added indentation and newlines will be passed on down
 * the filter chain (if any).</p>
 *
 * <p>In general, all whitespace in an XML document is potentially
 * significant, so a general-purpose XML writing tool like the
 * {@link XMLWriter} class cannot
 * add newlines or indentation.</p>
 *
 * <p>There is, however, a large class of XML documents where information
 * is strictly fielded: each element contains either character data
 * or other elements, but not both.  For this special case, it is possible
 * for a writing tool to provide automatic indentation and newlines
 * without requiring extra work from the user.  Note that this class
 * will likely not yield appropriate results for document-oriented
 * XML like XHTML pages, which mix character data and elements together.</p>
 *
 * <p>This writer will automatically place each start tag on a new line,
 * optionally indented if an indent step is provided (by default, there
 * is no indentation).  If an element contains other elements, the end
 * tag will also appear on a new line with leading indentation.  Consider,
 * for example, the following code:</p>
 *
 * <pre>
 * DataWriter w = new DataWriter();
 *
 * w.setIndentStep(2);
 * w.startDocument();
 * w.startElement("Person");
 * w.dataElement("name", "Jane Smith");
 * w.dataElement("date-of-birth", "1965-05-23");
 * w.dataElement("citizenship", "US");
 * w.endElement("Person");
 * w.endDocument();
 * </pre>
 *
 * <p>This code will produce the following document:</p>
 *
 * <pre>{@code
 * <?xml version="1.0" standalone="yes"?>
 *
 * <Person>
 *   <name>Jane Smith</name>
 *   <date-of-birth>1965-05-23</date-of-birth>
 *   <citizenship>US</citizenship>
 * </Person>
 * }</pre>
 *
 * <p>This class inherits from {@link XMLWriter},
 * and provides all of the same support for Namespaces.</p>
 *
 * @since 1.0
 * @author David Megginson, david@megginson.com
 * @version 0.2
 * @see XMLWriter
 */
public class DataWriter extends XMLWriter
{



    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////


    /**
     * Create a new data writer for the specified output.
     *
     * @param writer The character stream where the XML document
     *        will be written.
     * @param encoding 
     *      If non-null string is specified, it is written as a part
     *      of the XML declaration.
     */
    public DataWriter ( Writer writer, String encoding, CharacterEscapeHandler _escapeHandler )
    {
        super(writer,encoding,_escapeHandler);
    }


    public DataWriter (Writer writer, String encoding ) {
        this( writer, encoding, DumbEscapeHandler.theInstance ); 
    }



    ////////////////////////////////////////////////////////////////////
    // Accessors and setters.
    ////////////////////////////////////////////////////////////////////


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
        StringBuilder buf = new StringBuilder();
        for( ; indentStep>0; indentStep-- )
            buf.append(' ');
        setIndentStep(buf.toString());
    }
    
    public void setIndentStep(String s) {
        this.indentStep = s;
    }



    ////////////////////////////////////////////////////////////////////
    // Override methods from XMLWriter.
    ////////////////////////////////////////////////////////////////////


    /**
     * Reset the writer so that it can be reused.
     *
     * <p>This method is especially useful if the writer failed
     * with an exception the last time through.</p>
     *
     * @see XMLWriter#reset()
     */
    public void reset ()
    {
        depth = 0;
        state = SEEN_NOTHING;
        stateStack = new Stack<Object>();
        super.reset();
    }

    protected void writeXmlDecl(String decl) throws IOException {
        super.writeXmlDecl(decl);
        write('\n');
    }


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
     * @see XMLWriter#startElement(String, String, String, Attributes)
     */
    public void startElement (String uri, String localName,
                              String qName, Attributes atts)
        throws SAXException
    {
        stateStack.push(SEEN_ELEMENT);
        state = SEEN_NOTHING;
        if (depth > 0) {
            super.characters("\n");
        }
        doIndent();
        super.startElement(uri, localName, qName, atts);
        depth++;
    }


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
            super.characters("\n");
            doIndent();
        }
        super.endElement(uri, localName, qName);
        state = stateStack.pop();
    }

    public void endDocument() throws SAXException {
        try {
            write('\n');
        } catch( IOException e ) {
            throw new SAXException(e);
        }
        super.endDocument();
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

// end of DataWriter.java
