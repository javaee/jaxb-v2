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

package com.sun.xml.bind.marshaller;

import com.sun.xml.bind.util.AttributesImpl;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This filter will insert the xsi:schemaLocation and 
 * xsi:noNamespaceSchemaLocation attributes on the root
 * element of the marshalled xml if the properties are
 * set on the javax.xml.bind.Marshaller.  It will modify
 * the namespace prefix if necessary to avoid a collision
 * with an existing "xsi" prefix that doesn't point to the
 * XMLSchema-Instance uri.
 * 
 * If the client needs to have finer grained control over
 * where these attributes appear in the marshalled xml data,
 * then they have to write their own filter to add the values.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.2.6.1 $
 * @since JAXB1.0
 * @deprecated not used in 2.0
 */
public class SchemaLocationFilter extends XMLFilterImpl {

    // the schemaLocation values
    private final String schemaLocation;
    private final String noNSSchemaLocation;
    
    // only set the schemaLocation attributes on the root element
    private boolean rootElement = true;
    
    // remember the namespace URI to which the "xsi" prefix is bound to, if it already exists
    private String seenXsiURI = null;
    
    // the prefix that will be used when declaring the schemaLocation atts
    private String prefix = "xsi";
    
    // set to true if we actually insert a new declaration into the stream
    private boolean prefixDeclared = false;
    
    // the namespace uri of the schemaLocation atts
    private String xsiURI = "http://www.w3.org/2001/XMLSchema-instance";
    
    // keep track of the number of elements so we know when we're processing
    // the last one.
    private int elementCount = 0;
    
    public SchemaLocationFilter( String _schemaLocation, String _noNSSchemaLocation, ContentHandler _writer ) {
        schemaLocation = _schemaLocation;
        noNSSchemaLocation = _noNSSchemaLocation;
        setContentHandler( _writer );
    }
    
    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement( String namespaceURI, String localName, 
                               String qname, Attributes atts)
        throws SAXException {
            
        elementCount++;
        
        if( rootElement ) {
            AttributesImpl attributes = new AttributesImpl( atts );
            atts = attributes;
                            
            // see if "xsi" already exists and has been set to something else                
            if( seenXsiURI != null && seenXsiURI.equals( xsiURI ) ) {
                // no need to declare the prefix
            } else {
                if( seenXsiURI!=null )
                    // make up a new prefix
                    prefix = "xmlschemainstance";
                    
                prefixDeclared = true;
                
                // declare the namespace
                super.startPrefixMapping( prefix, xsiURI );
            }
            
            // set the schemaLocation attribute
            if( schemaLocation != null ) {
                attributes.addAttribute( xsiURI, "schemaLocation",
                                         prefix + ":schemaLocation",
                                         "CDATA",
                                         schemaLocation );
            }
            
            // set the noNSSchemaLocation attribute
            if( noNSSchemaLocation != null ) {
                attributes.addAttribute( xsiURI, "noNamespaceSchemaLocation",
                                         prefix + ":noNamespaceSchemaLocation",
                                         "CDATA",
                                         noNSSchemaLocation );
            }
        
            // remember that we've seen the root element    
            rootElement = false;
        }
        
        // forward the event with the new attributes
        super.startElement( namespaceURI, localName, qname, atts );
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement( String namespaceURI, String localName, 
                             String qname)
        throws SAXException {

        // check to see if this is the last element and we need to undeclare the prefix
        if( --elementCount == 0 && prefixDeclared ) {
            super.endPrefixMapping( prefix );
        }            
        
        // forward the event
        super.endElement( namespaceURI, localName, qname );
    }
    
    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping( String prefix, String uri )
        throws SAXException {
            
        // look for "xsi" namespace prefix to see if it is already declared
        if( "xsi".equals( prefix ) ) {
            seenXsiURI = uri;
        }
        
        // forward the event        
        super.startPrefixMapping( prefix, uri );
    }

}
