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

package com.sun.tools.xjc.reader.internalizer;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * {@link XMLReader} filter for supporting
 * <tt>http://xml.org/sax/features/namespace-prefixes</tt> feature.
 *
 * @author Kohsuke Kawaguchi
 */
final class ContentHandlerNamespacePrefixAdapter extends XMLFilterImpl {
    /**
     * True if <tt>http://xml.org/sax/features/namespace-prefixes</tt> is set to true.
     */
    private boolean namespacePrefixes = false;

    private String[] nsBinding = new String[8];
    private int len;

    public ContentHandlerNamespacePrefixAdapter() {
    }

    public ContentHandlerNamespacePrefixAdapter(XMLReader parent) {
        setParent(parent);
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if(name.equals(PREFIX_FEATURE))
            return namespacePrefixes;
        return super.getFeature(name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if(name.equals(PREFIX_FEATURE)) {
            this.namespacePrefixes = value;
            return;
        }
        if(name.equals(NAMESPACE_FEATURE) && value)
            return;
        super.setFeature(name, value);
    }


    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if(len==nsBinding.length) {
            // reallocate
            String[] buf = new String[nsBinding.length*2];
            System.arraycopy(nsBinding,0,buf,0,nsBinding.length);
            nsBinding = buf;
        }
        nsBinding[len++] = prefix;
        nsBinding[len++] = uri;
        super.startPrefixMapping(prefix,uri);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if(namespacePrefixes) {
            this.atts.setAttributes(atts);
            // add namespace bindings back as attributes
            for( int i=0; i<len; i+=2 ) {
                String prefix = nsBinding[i];
                if(prefix.length()==0)
                    this.atts.addAttribute(XMLConstants.XML_NS_URI,"xmlns","xmlns","CDATA",nsBinding[i+1]);
                else
                    this.atts.addAttribute(XMLConstants.XML_NS_URI,prefix,"xmlns:"+prefix,"CDATA",nsBinding[i+1]);
            }
            atts = this.atts;
        }
        len=0;
        super.startElement(uri, localName, qName, atts);
    }

    private final AttributesImpl atts = new AttributesImpl();

    private static final String PREFIX_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
    private static final String NAMESPACE_FEATURE = "http://xml.org/sax/features/namespaces";
}
