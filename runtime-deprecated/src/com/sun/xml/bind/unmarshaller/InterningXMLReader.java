/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.bind.unmarshaller;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * {@link XMLReader} that interns all the string constants before
 * calling the {@link org.xml.sax.ContentHandler}. 
 * 
 * <p>
 * Most of the parsers out there (at least Crimson and Xerces) supports
 * SAX <tt>http://xml.org/sax/features/string-interning</tt> feature,
 * but if the parser doesn't support it (or if the SAX events is read
 * from components other than {@link XMLReader}, this adaptor is used
 * to make all strings interned.
 * </p> 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @since 
 *     JAXB RI 1.0.3
 */
public class InterningXMLReader extends XMLFilterImpl {
    
    /**
     * Wraps the given {@link XMLReader} (if necessary) so that
     * it performs string interning.
     */
    public static XMLReader adapt( XMLReader reader ) {
        try {
            if( reader.getFeature("http://xml.org/sax/features/string-interning") )
                return reader;  // no need for wrapping
        } catch (SAXException e) {
            ; // unrecognized/unsupported
        }
        // otherwise we have to wrap
        return new InterningXMLReader(reader);
    }
    
    protected InterningXMLReader( XMLReader core ) {
        super(core);
    }
    
    protected InterningXMLReader() {
        super();
    }
    
    private final AttributesImpl attributes = new AttributesImpl();
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(intern(uri), intern(localName), intern(qName));
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        super.endPrefixMapping(intern(prefix));
    }

    public void processingInstruction(String target, String data) throws SAXException {
        super.processingInstruction(intern(target), intern(data));
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        attributes.setAttributes(atts);
        super.startElement(intern(uri), intern(localName), intern(qName), attributes);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(intern(prefix), intern(uri));
    }

    private static String intern(String s) {
        if(s==null)     return null;
        else            return s.intern();
    }
    
    
    
    private static class AttributesImpl implements Attributes {
        private Attributes core;

        void setAttributes(Attributes att) {
            this.core = att;
        }
        
        public int getIndex(String qName) {
            return core.getIndex(qName);
        }

        public int getIndex(String uri, String localName) {
            return core.getIndex(uri, localName);
        }

        public int getLength() {
            return core.getLength();
        }

        public String getLocalName(int index) {
            return intern(core.getLocalName(index));
        }

        public String getQName(int index) {
            return intern(core.getQName(index));
        }

        public String getType(int index) {
            return intern(core.getType(index));
        }

        public String getType(String qName) {
            return intern(core.getType(qName));
        }

        public String getType(String uri, String localName) {
            return intern(core.getType(uri, localName));
        }

        public String getURI(int index) {
            return intern(core.getURI(index));
        }
        
        //
        // since values may vary a lot,
        // we don't (probably shouldn't) intern values.
        //
        
        public String getValue(int index) {
            return core.getValue(index);
        }

        public String getValue(String qName) {
            return core.getValue(qName);
        }

        public String getValue(String uri, String localName) {
            return core.getValue(uri, localName);
        }
    }
}
