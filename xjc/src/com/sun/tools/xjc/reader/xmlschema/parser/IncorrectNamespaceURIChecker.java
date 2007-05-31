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
package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.tools.xjc.reader.Const;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This filter detects the use of incorrect JAXB namespace URI.
 * 
 * When the binding compiler looks at a schema file, it always look
 * for the namespace URI of the elements (which is correct, BTW.)
 * 
 * <p>
 * However, one unfortunate downside of this philosophically correct
 * behavior is that there is no provision or safety check when an user
 * misspelled JAXB binding customization namespace.
 * 
 * <p>
 * This checker inspects the input document and look for the use of the
 * prefix "jaxb". If the document doesn't associate any prefix to the
 * JAXB customization URI and if it does associate the jaxb prefix,
 * this checker will issue a warning.
 * 
 * <p>
 * This warning can happen to completely correct schema (because
 * nothing prevents you from using the prefix "jaxb" for other purpose
 * while using a JAXB compiler on the same schema) but in practice
 * this would be quite unlikely.
 * 
 * <p>
 * This justifies the use of this filter.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class IncorrectNamespaceURIChecker extends XMLFilterImpl {

    public IncorrectNamespaceURIChecker( ErrorHandler handler ) {
        this.errorHandler = handler;
    }
    
    private ErrorHandler errorHandler;
    
    private Locator locator = null;
    
    /** Sets to true once we see the jaxb prefix in use. */
    private boolean isJAXBPrefixUsed = false;
    /** Sets to true once we see the JAXB customization namespace URI. */ 
    private boolean isCustomizationUsed = false;
    
    public void endDocument() throws SAXException {
        if( isJAXBPrefixUsed && !isCustomizationUsed ) {
            SAXParseException e = new SAXParseException(
                Messages.format(Messages.WARN_INCORRECT_URI, Const.JAXB_NSURI),
                locator );
            errorHandler.warning(e);
        }
        
        super.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if( prefix.equals("jaxb") )
            isJAXBPrefixUsed = true;
        if( uri.equals(Const.JAXB_NSURI) )
            isCustomizationUsed = true;
        
        super.startPrefixMapping(prefix, uri);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        super.startElement(namespaceURI, localName, qName, atts);
        
        // I'm not sure if this is necessary (SAX might report the change of the default prefix
        // through the startPrefixMapping method, and I think it does indeed.)
        // 
        // but better safe than sorry.
        
        if( namespaceURI.equals(Const.JAXB_NSURI) )
            isCustomizationUsed = true;
    }

    public void setDocumentLocator( Locator locator ) {
        super.setDocumentLocator( locator );
        this.locator = locator;
    }
}
