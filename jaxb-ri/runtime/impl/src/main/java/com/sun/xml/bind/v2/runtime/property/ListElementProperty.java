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

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;

import org.xml.sax.SAXException;

/**
 * {@link Property} implementation for {@link ElementPropertyInfo} whose
 * {@link ElementPropertyInfo#isValueList()} is true.
 *
 * @author Kohsuke Kawaguchi
 */
final class ListElementProperty<BeanT,ListT,ItemT> extends ArrayProperty<BeanT,ListT,ItemT> {

    private final Name tagName;
    private final String defaultValue;
    
    /**
     * Converts all the values to a list and back.
     */
    private final TransducedAccessor<BeanT> xacc;

    public ListElementProperty(JAXBContextImpl grammar, RuntimeElementPropertyInfo prop) {
        super(grammar, prop);

        assert prop.isValueList();
        assert prop.getTypes().size()==1;   // required by the contract of isValueList
        RuntimeTypeRef ref = prop.getTypes().get(0);

        tagName = grammar.nameBuilder.createElementName(ref.getTagName());
        defaultValue = ref.getDefaultValue();
        
        // transducer for each item
        Transducer xducer = ref.getTransducer();
        // transduced accessor for the whole thing
        xacc = new ListTransducedAccessorImpl(xducer,acc,lister);
    }

    public PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
        Loader l = new LeafPropertyLoader(xacc);
        l = new DefaultValueLoaderDecorator(l, defaultValue);
        handlers.put(tagName, new ChildLoader(l,null));
    }

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ListT list = acc.get(o);

        if(list!=null) {
            if(xacc.useNamespace()) {
                w.startElement(tagName,null);
                xacc.declareNamespace(o,w);
                w.endNamespaceDecls(list);
                w.endAttributes();
                xacc.writeText(w,o,fieldName);
                w.endElement();
            } else {
                xacc.writeLeafElement(w, tagName, o, fieldName);
            }
        }
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(tagName!=null) {
            if(tagName.equals(nsUri,localName))
                return acc;
        }
        return null;
    }
}
