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

package com.sun.tools.xjc.reader.internalizer;

import com.sun.xml.bind.WhiteSpaceProcessor;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Strips ignorable whitespace from SAX event stream.
 * 
 * <p>
 * This filter works only when the event stream doesn't
 * contain any mixed content.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class WhitespaceStripper extends XMLFilterImpl {

    private int state = 0;
    
    private char[] buf = new char[1024];
    private int bufLen = 0;
    
    private static final int AFTER_START_ELEMENT = 1;
    private static final int AFTER_END_ELEMENT = 2;

    public WhitespaceStripper(XMLReader reader) {
        setParent(reader);
    }

    public WhitespaceStripper(ContentHandler handler,ErrorHandler eh,EntityResolver er) {
        setContentHandler(handler);
        if(eh!=null)    setErrorHandler(eh);
        if(er!=null)    setEntityResolver(er);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        switch(state) {
        case AFTER_START_ELEMENT:
            // we have to store the characters here, even if it consists entirely
            // of whitespaces. This is because successive characters event might
            // include non-whitespace char, in which case all the whitespaces in
            // this event may suddenly become significant.
            if( bufLen+length>buf.length ) {
                // reallocate buffer
                char[] newBuf = new char[Math.max(bufLen+length,buf.length*2)];
                System.arraycopy(buf,0,newBuf,0,bufLen);
                buf = newBuf;
            }
            System.arraycopy(ch,start,buf,bufLen,length);
            bufLen += length;
            break;
        case AFTER_END_ELEMENT:
            // check if this is ignorable.
            int len = start+length;
            for( int i=start; i<len; i++ )
                if( !WhiteSpaceProcessor.isWhiteSpace(ch[i]) ) {
                    super.characters(ch, start, length);
                    return;
                }
            // if it's entirely whitespace, ignore it.
            break;
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        processPendingText();
        super.startElement(uri, localName, qName, atts);
        state = AFTER_START_ELEMENT;
        bufLen = 0;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processPendingText();
        super.endElement(uri, localName, qName);
        state = AFTER_END_ELEMENT;
    }
    
    /**
     * Forwars the buffered characters if it contains any non-whitespace
     * character.
     */
    private void processPendingText() throws SAXException {
        if(state==AFTER_START_ELEMENT) {
            for( int i=bufLen-1; i>=0; i-- )
                if( !WhiteSpaceProcessor.isWhiteSpace(buf[i]) ) {
                    super.characters(buf, 0, bufLen);
                    return;
               }
        }
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // ignore completely.
    }
}
