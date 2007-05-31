/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
