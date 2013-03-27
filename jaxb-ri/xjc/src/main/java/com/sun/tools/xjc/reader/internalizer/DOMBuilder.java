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

import java.util.Set;

import com.sun.tools.xjc.reader.Const;
import com.sun.xml.bind.marshaller.SAX2DOMEx;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Builds DOM while keeping the location information.
 * 
 * <p>
 * This class also looks for outer most &lt;jaxb:bindings>
 * customizations.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class DOMBuilder extends SAX2DOMEx {
    /**
     * Grows a DOM tree under the given document, and
     * stores location information to the given table.
     * 
     * @param outerMostBindings
     *      This set will receive newly found outermost
     *      jaxb:bindings customizations.
     */
    public DOMBuilder( Document dom, LocatorTable ltable, Set outerMostBindings ) {
        super( dom );
        this.locatorTable = ltable;
        this.outerMostBindings = outerMostBindings;
    }
    
    /** Location information will be stored into this object. */
    private final LocatorTable locatorTable;
    
    private final Set outerMostBindings;
    
    private Locator locator;
    
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        super.setDocumentLocator(locator);
    }
    

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        super.startElement(namespaceURI, localName, qName, atts);
        
        Element e = getCurrentElement();
        locatorTable.storeStartLocation( e, locator );
        
        // check if this element is an outer-most <jaxb:bindings>
        if( Const.JAXB_NSURI.equals(e.getNamespaceURI())
        &&  "bindings".equals(e.getLocalName()) ) {
            
            // if this is the root node (meaning that this file is an
            // external binding file) or if the parent is XML Schema element
            // (meaning that this is an "inlined" external binding)
            Node p = e.getParentNode();
            if( p instanceof Document
            ||( p instanceof Element && !e.getNamespaceURI().equals(p.getNamespaceURI()))) {
                outerMostBindings.add(e);   // remember this value
            }
        }
    }
    
    public void endElement(String namespaceURI, String localName, String qName) {
        locatorTable.storeEndLocation( getCurrentElement(), locator );
        super.endElement(namespaceURI, localName, qName);
    }
}
