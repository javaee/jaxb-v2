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

package com.sun.xml.bind.v2.runtime.property;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.bind.v2.util.QNameMap;

import javax.xml.bind.JAXBElement;
import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class SingleElementNodeProperty<BeanT,ValueT> extends PropertyImpl<BeanT> {

    private final Accessor<BeanT,ValueT> acc;

    private final boolean nillable;

    private final QName[] acceptedElements;

    private final Map<Class,TagAndType> typeNames = new HashMap<Class,TagAndType>();

    private RuntimeElementPropertyInfo prop;
    
    /**
     * The tag name used to produce xsi:nil. The first one in the list.
     */
    private final Name nullTagName;

    public SingleElementNodeProperty(JAXBContextImpl context, RuntimeElementPropertyInfo prop) {
        super(context,prop);
        acc = prop.getAccessor().optimize(context);
        this.prop = prop;

        QName nt = null;
        boolean nil = false;

        acceptedElements = new QName[prop.getTypes().size()];
        for( int i=0; i<acceptedElements.length; i++ )
            acceptedElements[i] = prop.getTypes().get(i).getTagName();

        for (RuntimeTypeRef e : prop.getTypes()) {
            JaxBeanInfo beanInfo = context.getOrCreate(e.getTarget());
            if(nt==null)    nt = e.getTagName();
            typeNames.put( beanInfo.jaxbType, new TagAndType(
                context.nameBuilder.createElementName(e.getTagName()),beanInfo) );
            nil |= e.isNillable();
        }
        
        nullTagName = context.nameBuilder.createElementName(nt);

        nillable = nil;
    }

    @Override
    public void wrapUp() {
        super.wrapUp();
        prop = null;
    }

    public void reset(BeanT bean) throws AccessorException {
        acc.set(bean,null);
    }

    public String getIdValue(BeanT beanT) {
        return null;
    }

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ValueT v = acc.get(o);
        if (v!=null) {
            Class vtype = v.getClass();
            TagAndType tt=typeNames.get(vtype); // quick way that usually works

            if(tt==null) {// slow way that always works
                for (Map.Entry<Class,TagAndType> e : typeNames.entrySet()) {
                    if(e.getKey().isAssignableFrom(vtype)) {
                        tt = e.getValue();
                        break;
                    }
                }
            }

            boolean addNilDecl = (o instanceof JAXBElement) && ((JAXBElement)o).isNil();
            if(tt==null) {
                // actually this is an error, because the actual type was not a sub-type
                // of any of the types specified in the annotations,
                // but for the purpose of experimenting with simple type substitution,
                // it's convenient to marshal this anyway (for example so that classes
                // generated from simple types like String can be marshalled as expected.)
                w.startElement(typeNames.values().iterator().next().tagName,null);
                w.childAsXsiType(v,fieldName,w.grammar.getBeanInfo(Object.class), addNilDecl && nillable);
            } else {
                w.startElement(tt.tagName,null);
                w.childAsXsiType(v,fieldName,tt.beanInfo, addNilDecl && nillable);
            }
            w.endElement();
        } else if (nillable) {
            w.startElement(nullTagName,null);
            w.writeXsiNilTrue();
            w.endElement();
        }
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
        JAXBContextImpl context = chain.context;

        for (TypeRef<Type,Class> e : prop.getTypes()) {
            JaxBeanInfo bi = context.getOrCreate((RuntimeTypeInfo) e.getTarget());
            // if the expected Java type is already final, type substitution won't really work anyway.
            // this also traps cases like trying to substitute xsd:long element with xsi:type='xsd:int'
            Loader l = bi.getLoader(context,!Modifier.isFinal(bi.jaxbType.getModifiers()));
            if(e.getDefaultValue()!=null)
                l = new DefaultValueLoaderDecorator(l,e.getDefaultValue());
            if(nillable || chain.context.allNillable)
                l = new XsiNilLoader.Single(l,acc);
            handlers.put( e.getTagName(), new ChildLoader(l,acc));
        }
    }

    public PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        for( QName n : acceptedElements) {
            if(n.getNamespaceURI().equals(nsUri) && n.getLocalPart().equals(localName))
                return acc;
        }
        return null;
    }

}
