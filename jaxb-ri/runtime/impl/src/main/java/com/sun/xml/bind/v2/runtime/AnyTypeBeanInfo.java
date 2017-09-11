/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.ValidationEvent;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.DomLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} for handling {@code xs:anyType}.
 *
 * @author Kohsuke Kawaguchi
 */
final class AnyTypeBeanInfo extends JaxBeanInfo<Object> implements AttributeAccessor {

    private boolean nilIncluded = false;
    
    public AnyTypeBeanInfo(JAXBContextImpl grammar,RuntimeTypeInfo anyTypeInfo) {
        super(grammar, anyTypeInfo, Object.class, new QName(WellKnownNamespace.XML_SCHEMA,"anyType"), false, true, false);
    }

    public String getElementNamespaceURI(Object element) {
        throw new UnsupportedOperationException();
    }

    public String getElementLocalName(Object element) {
        throw new UnsupportedOperationException();
    }

    public Object createInstance(UnmarshallingContext context) {
        throw new UnsupportedOperationException();
        // return JAXBContextImpl.createDom().createElementNS("","noname");
    }

    public boolean reset(Object element, UnmarshallingContext context) {
        return false;
//        NodeList nl = element.getChildNodes();
//        while(nl.getLength()>0)
//            element.removeChild(nl.item(0));
//        NamedNodeMap al = element.getAttributes();
//        while(al.getLength()>0)
//            element.removeAttributeNode((Attr)al.item(0));
//        return true;
    }

    public String getId(Object element, XMLSerializer target) {
        return null;
    }

    public void serializeBody(Object element, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        NodeList childNodes = ((Element)element).getChildNodes();
        int len = childNodes.getLength();
        for( int i=0; i<len; i++ ) {
            Node child = childNodes.item(i);
            switch(child.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                target.text(child.getNodeValue(),null);
                break;
            case Node.ELEMENT_NODE:
                target.writeDom((Element)child,domHandler,null,null);
                break;
            }
        }
    }

    public void serializeAttributes(Object element, XMLSerializer target) throws SAXException {
        NamedNodeMap al = ((Element)element).getAttributes();
        int len = al.getLength();
        for( int i=0; i<len; i++ ) {
            Attr a = (Attr)al.item(i);
            // work defensively
            String uri = a.getNamespaceURI();
            if(uri==null)   uri="";
            String local = a.getLocalName();
            String name = a.getName();
            if(local==null) local = name;
            if (uri.equals(WellKnownNamespace.XML_SCHEMA_INSTANCE) && ("nil".equals(local))) {
                isNilIncluded = true;
            }
            if(name.startsWith("xmlns")) continue;// DOM reports ns decls as attributes

            target.attribute(uri,local,a.getValue());
        }
    }

    public void serializeRoot(Object element, XMLSerializer target) throws SAXException {
        target.reportError(
                new ValidationEventImpl(
                        ValidationEvent.ERROR,
                        Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(element.getClass().getName()),
                        null,
                        null));
    }

    public void serializeURIs(Object element, XMLSerializer target) {
        NamedNodeMap al = ((Element)element).getAttributes();
        int len = al.getLength();
        NamespaceContext2 context = target.getNamespaceContext();
        for( int i=0; i<len; i++ ) {
            Attr a = (Attr)al.item(i);
            if ("xmlns".equals(a.getPrefix())) {
                context.force(a.getValue(), a.getLocalName());
                continue;
            }
            if ("xmlns".equals(a.getName())) {
                if (element instanceof org.w3c.dom.Element) {
                    context.declareNamespace(a.getValue(), null, false);
                    continue;
                } else {
                    context.force(a.getValue(), "");
                    continue;
                }
            }
            String nsUri = a.getNamespaceURI();
            if(nsUri!=null && nsUri.length()>0)
                context.declareNamespace( nsUri, a.getPrefix(), true );
        }
    }

    public Transducer<Object> getTransducer() {
        return null;
    }

    public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        if(typeSubstitutionCapable)
            return substLoader;
        else
            return domLoader;
    }

    private static final W3CDomHandler domHandler = new W3CDomHandler();
    private static final DomLoader domLoader = new DomLoader(domHandler);
    private final XsiTypeLoader substLoader = new XsiTypeLoader(this);

    }
