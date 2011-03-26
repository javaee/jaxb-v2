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

package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.tools.xjc.reader.internalizer.AbstractReferenceFinderImpl;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import com.sun.tools.xjc.util.DOMUtils;
import com.sun.xml.bind.v2.WellKnownNamespace;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * XML Schema specific internalization logic.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XMLSchemaInternalizationLogic implements InternalizationLogic {

    /**
     * This filter looks for &lt;xs:import> and &lt;xs:include>
     * and parses those documents referenced by them.
     */
    private static final class ReferenceFinder extends AbstractReferenceFinderImpl {
        ReferenceFinder( DOMForest parent ) {
            super(parent);
        }
        
        protected String findExternalResource( String nsURI, String localName, Attributes atts) {
            if( WellKnownNamespace.XML_SCHEMA.equals(nsURI)
            && ("import".equals(localName) || "include".equals(localName) ) )
                return atts.getValue("schemaLocation");
            else
                return null;
        }
    }

    public XMLFilterImpl createExternalReferenceFinder(DOMForest parent) {
        return new ReferenceFinder(parent);
    }

    public boolean checkIfValidTargetNode(DOMForest parent, Element bindings, Element target) {
        return WellKnownNamespace.XML_SCHEMA.equals(target.getNamespaceURI());
    }

    public Element refineTarget(Element target) {
        // look for existing xs:annotation
        Element annotation = DOMUtils.getFirstChildElement(target, WellKnownNamespace.XML_SCHEMA, "annotation");
        if(annotation==null)
            // none exists. need to make one
            annotation = insertXMLSchemaElement( target, "annotation" );
        
        // then look for appinfo
        Element appinfo = DOMUtils.getFirstChildElement(annotation, WellKnownNamespace.XML_SCHEMA, "appinfo" );
        if(appinfo==null)
            // none exists. need to make one
            appinfo = insertXMLSchemaElement( annotation, "appinfo" );
        
        return appinfo;
    }

    /**
     * Creates a new XML Schema element of the given local name
     * and insert it as the first child of the given parent node.
     * 
     * @return
     *      Newly create element.
     */
    private Element insertXMLSchemaElement( Element parent, String localName ) {
        // use the same prefix as the parent node to avoid modifying
        // the namespace binding.
        String qname = parent.getTagName();
        int idx = qname.indexOf(':');
        if(idx==-1)     qname = localName;
        else            qname = qname.substring(0,idx+1)+localName;
        
        Element child = parent.getOwnerDocument().createElementNS( WellKnownNamespace.XML_SCHEMA, qname );
        
        NodeList children = parent.getChildNodes();
        
        if( children.getLength()==0 )
            parent.appendChild(child);
        else
            parent.insertBefore( child, children.item(0) );
        
        return child;
    }
}
