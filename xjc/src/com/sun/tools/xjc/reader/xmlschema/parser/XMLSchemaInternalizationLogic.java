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
