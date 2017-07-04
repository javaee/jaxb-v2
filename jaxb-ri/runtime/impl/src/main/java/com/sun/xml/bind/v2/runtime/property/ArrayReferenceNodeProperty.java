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

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.WildcardLoader;
import com.sun.xml.bind.v2.util.QNameMap;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
class ArrayReferenceNodeProperty<BeanT,ListT,ItemT> extends ArrayERProperty<BeanT,ListT,ItemT> {

    /**
     * Expected element names and what class to unmarshal.
     */
    private final QNameMap<JaxBeanInfo> expectedElements = new QNameMap<JaxBeanInfo>();

    private final boolean isMixed;

    private final DomHandler domHandler;
    private final WildcardMode wcMode;

    public ArrayReferenceNodeProperty(JAXBContextImpl p, RuntimeReferencePropertyInfo prop) {
        super(p, prop, prop.getXmlName(), prop.isCollectionNillable());

        for (RuntimeElement e : prop.getElements()) {
            JaxBeanInfo bi = p.getOrCreate(e);
            expectedElements.put( e.getElementName().getNamespaceURI(),e.getElementName().getLocalPart(), bi );
        }

        isMixed = prop.isMixed();

        if(prop.getWildcard()!=null) {
            domHandler = (DomHandler) ClassFactory.create(prop.getDOMHandler());
            wcMode = prop.getWildcard();
        } else {
            domHandler = null;
            wcMode = null;
        }
    }

    protected final void serializeListBody(BeanT o, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException {
        ListIterator<ItemT> itr = lister.iterator(list, w);

        while(itr.hasNext()) {
            try {
                ItemT item = itr.next();
                if (item != null) {
                    if(isMixed && item.getClass()==String.class) {
                        w.text((String)item,null);
                    } else {
                        JaxBeanInfo bi = w.grammar.getBeanInfo(item,true);
                        if(bi.jaxbType==Object.class && domHandler!=null)
                            // even if 'v' is a DOM node, it always derive from Object,
                            // so the getBeanInfo returns BeanInfo for Object
                            w.writeDom(item,domHandler,o,fieldName);
                        else
                            bi.serializeRoot(item,w);
                    }
                }
            } catch (JAXBException e) {
                w.reportError(fieldName,e);
                // recover by ignoring this item
            }
        }
    }

    public void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<ChildLoader> loaders) {
        final int offset = chain.allocateOffset();

        Receiver recv = new ReceiverImpl(offset);

        for( QNameMap.Entry<JaxBeanInfo> n : expectedElements.entrySet() ) {
            final JaxBeanInfo beanInfo = n.getValue();
            loaders.put(n.nsUri,n.localName,new ChildLoader(beanInfo.getLoader(chain.context,true),recv));
        }

        if(isMixed) {
            // handler for processing mixed contents.
            loaders.put(TEXT_HANDLER,
                new ChildLoader(new MixedTextLoader(recv),null));
        }

        if(domHandler!=null) {
            loaders.put(CATCH_ALL,
                new ChildLoader(new WildcardLoader(domHandler,wcMode),recv));
        }
    }

    private static final class MixedTextLoader extends Loader {

        private final Receiver recv;

        public MixedTextLoader(Receiver recv) {
            super(true);
            this.recv = recv;
        }

        public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
            if(text.length()!=0) // length 0 text is pointless
                recv.receive(state,text.toString());
        }
    }


    public PropertyKind getKind() {
        return PropertyKind.REFERENCE;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        // TODO: unwrap JAXBElement
        if(wrapperTagName!=null) {
            if(wrapperTagName.equals(nsUri,localName))
                return acc;
        } else {
            if(expectedElements.containsKey(nsUri,localName))
                return acc;
        }
        return null;
    }
}
