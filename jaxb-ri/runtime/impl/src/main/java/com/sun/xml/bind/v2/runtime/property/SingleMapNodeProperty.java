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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeMapPropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.State;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
final class SingleMapNodeProperty<BeanT,ValueT extends Map> extends PropertyImpl<BeanT> {

    private final Accessor<BeanT,ValueT> acc;
    /**
     * The tag name that surrounds the whole property.
     */
    private final Name tagName;
    /**
     * The tag name that corresponds to the 'entry' element.
     */
    private final Name entryTag;
    private final Name keyTag;
    private final Name valueTag;

    private final boolean nillable;

    private JaxBeanInfo keyBeanInfo;
    private JaxBeanInfo valueBeanInfo;

    /**
     * The implementation class for this property.
     * If the property is null, we create an instance of this class.
     */
    private final Class<? extends ValueT> mapImplClass;

    public SingleMapNodeProperty(JAXBContextImpl context, RuntimeMapPropertyInfo prop) {
        super(context, prop);
        acc = prop.getAccessor().optimize(context);
        this.tagName = context.nameBuilder.createElementName(prop.getXmlName());
        this.entryTag = context.nameBuilder.createElementName("","entry");
        this.keyTag = context.nameBuilder.createElementName("","key");
        this.valueTag = context.nameBuilder.createElementName("","value");
        this.nillable = prop.isCollectionNillable();
        this.keyBeanInfo = context.getOrCreate(prop.getKeyType());
        this.valueBeanInfo = context.getOrCreate(prop.getValueType());

        // infer the implementation class
        //noinspection unchecked
        Class<ValueT> sig = (Class<ValueT>) Utils.REFLECTION_NAVIGATOR.erasure(prop.getRawType());
        mapImplClass = ClassFactory.inferImplClass(sig,knownImplClasses);
        // TODO: error check for mapImplClass==null
        // what is the error reporting path for this part of the code?
    }

    private static final Class[] knownImplClasses = {
        HashMap.class, TreeMap.class, LinkedHashMap.class
    };

    public void reset(BeanT bean) throws AccessorException {
        acc.set(bean,null);
    }


    /**
     * A Map property can never be ID.
     */
    public String getIdValue(BeanT bean) {
        return null;
    }

    public PropertyKind getKind() {
        return PropertyKind.MAP;
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
        keyLoader = keyBeanInfo.getLoader(chain.context,true);
        valueLoader = valueBeanInfo.getLoader(chain.context,true);
        handlers.put(tagName,new ChildLoader(itemsLoader,null));
    }

    private Loader keyLoader;
    private Loader valueLoader;

    /**
     * Handles {@code <items>} and {@code </items>}.
     *
     * The target will be set to a {@link Map}.
     */
    private final Loader itemsLoader = new Loader(false) {

        private ThreadLocal<Stack<BeanT>> target = new ThreadLocal<Stack<BeanT>>();
        private ThreadLocal<Stack<ValueT>> map = new ThreadLocal<Stack<ValueT>>();

        @Override
        public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            // create or obtain the Map object
            try {
                BeanT target = (BeanT) state.getPrev().getTarget();
                ValueT mapValue = acc.get(target);
                if(mapValue == null)
                    mapValue = ClassFactory.create(mapImplClass);
                else
                    mapValue.clear();

                Stack.push(this.target, target);
                Stack.push(map, mapValue);
                state.setTarget(mapValue);
            } catch (AccessorException e) {
                // recover from error by setting a dummy Map that receives and discards the values
                handleGenericException(e,true);
                state.setTarget(new HashMap());
            }
        }

        @Override
        public void leaveElement(State state, TagName ea) throws SAXException {
            super.leaveElement(state, ea);
            try {
                acc.set(Stack.pop(target), Stack.pop(map));
            } catch (AccessorException ex) {
                handleGenericException(ex,true);
            }
        }

        @Override
        public void childElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            if(ea.matches(entryTag)) {
                state.setLoader(entryLoader);
            } else {
                super.childElement(state,ea);
            }
        }

        @Override
        public Collection<QName> getExpectedChildElements() {
            return Collections.singleton(entryTag.toQName());
        }
    };

    /**
     * Handles {@code <entry>} and {@code </entry>}.
     *
     * The target will be set to a {@link Map}.
     */
    private final Loader entryLoader = new Loader(false) {
        @Override
        public void startElement(UnmarshallingContext.State state, TagName ea) {
            state.setTarget(new Object[2]);  // this is inefficient
        }

        @Override
        public void leaveElement(UnmarshallingContext.State state, TagName ea) {
            Object[] keyValue = (Object[])state.getTarget();
            Map map = (Map) state.getPrev().getTarget();
            map.put(keyValue[0],keyValue[1]);
        }

        @Override
        public void childElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            if(ea.matches(keyTag)) {
                state.setLoader(keyLoader);
                state.setReceiver(keyReceiver);
                return;
            }
            if(ea.matches(valueTag)) {
                state.setLoader(valueLoader);
                state.setReceiver(valueReceiver);
                return;
            }
            super.childElement(state,ea);
        }

        @Override
        public Collection<QName> getExpectedChildElements() {
            return Arrays.asList(keyTag.toQName(),valueTag.toQName());
        }
    };

    private static final class ReceiverImpl implements Receiver {
        private final int index;
        public ReceiverImpl(int index) {
            this.index = index;
        }
        public void receive(UnmarshallingContext.State state, Object o) {
            ((Object[])state.getTarget())[index] = o;
        }
    }

    private static final Receiver keyReceiver = new ReceiverImpl(0);
    private static final Receiver valueReceiver = new ReceiverImpl(1);

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ValueT v = acc.get(o);
        if(v!=null) {
            bareStartTag(w,tagName,v);
            for( Map.Entry e : (Set<Map.Entry>)v.entrySet() ) {
                bareStartTag(w,entryTag,null);

                Object key = e.getKey();
                if(key!=null) {
                    w.startElement(keyTag,key);
                    w.childAsXsiType(key,fieldName,keyBeanInfo, false);
                    w.endElement();
                }

                Object value = e.getValue();
                if(value!=null) {
                    w.startElement(valueTag,value);
                    w.childAsXsiType(value,fieldName,valueBeanInfo, false);
                    w.endElement();
                }

                w.endElement();
            }
            w.endElement();
        } else
        if(nillable) {
            w.startElement(tagName,null);
            w.writeXsiNilTrue();
            w.endElement();
        }
    }

    private void bareStartTag(XMLSerializer w, Name tagName, Object peer) throws IOException, XMLStreamException, SAXException {
        w.startElement(tagName,peer);
        w.endNamespaceDecls(peer);
        w.endAttributes();
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(tagName.equals(nsUri,localName))
            return acc;
        return null;
    }

    private static final class Stack<T> {
        private Stack<T> parent;
        private T value;

        private Stack(Stack<T> parent, T value) {
            this.parent = parent;
            this.value = value;
        }

        private Stack(T value) {
            this.value = value;
        }

        private static <T> void push(ThreadLocal<Stack<T>> holder, T value) {
            Stack<T> parent = holder.get();
            if (parent == null)
                holder.set(new Stack<T>(value));
            else
                holder.set(new Stack<T>(parent, value));
        }

        private static <T> T pop(ThreadLocal<Stack<T>> holder) {
            Stack<T> current = holder.get();
            if (current.parent == null)
                holder.remove();
            else
                holder.set(current.parent);
            return current.value;
        }

    }
}
