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

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Surpresses ignorable whitespaces.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class IgnorableWhitespaceFilter extends XMLFilterImpl {

    private final StringBuffer buf = new StringBuffer();

    IgnorableWhitespaceFilter( XMLReader parent ) {
        setParent(parent);
    }

    IgnorableWhitespaceFilter() {
    }

    /**
     * Characters event might be fired in two or more consequtive events,
     * so we can't just decide if something is ignorable from one event.
     *
     * Buffer, and process it later.
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {

        buf.append(ch,start,length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        flushCharacters();
        super.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        flushCharacters();
        super.endElement(uri, localName, qName);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        flushCharacters();
        super.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        flushCharacters();
        super.skippedEntity(name);
    }

    private void flushCharacters() throws SAXException {
        if(!isIgnorable(buf))
            super.characters(buf.toString().toCharArray(),0,buf.length());
        buf.setLength(0);
    }


    private static boolean isIgnorable(StringBuffer buf) {
        for (int i = buf.length()-1; i >= 0; i--)
            if (!isWhitespace(buf.charAt(i)))
                return false;
        return true;
    }
    private static boolean isWhitespace( char ch ) {
        return ch==' ' || ch=='\t' || ch=='\r' || ch=='\n';
    }
}
