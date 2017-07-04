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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.RuntimeUtil;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.TextLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.bind.v2.util.QNameMap;

import org.xml.sax.SAXException;

/**
 * {@link Property} implementation for multi-value property that maps to an element.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ArrayElementProperty<BeanT,ListT,ItemT> extends ArrayERProperty<BeanT,ListT,ItemT> {

    private final Map<Class,TagAndType> typeMap  = new HashMap<Class,TagAndType>();
    /**
     * Set by the constructor and reset in the {@link #wrapUp()} method.
     */
    private Map<TypeRef<Type,Class>,JaxBeanInfo> refs = new HashMap<TypeRef<Type, Class>, JaxBeanInfo>();
    /**
     * Set by the constructor and reset in the {@link #wrapUp()} method.
     */
    protected RuntimeElementPropertyInfo prop;

    /**
     * Tag name used when we see null in the collection. Can be null.
     */
    private final Name nillableTagName;

    protected ArrayElementProperty(JAXBContextImpl grammar, RuntimeElementPropertyInfo prop) {
        super(grammar, prop, prop.getXmlName(), prop.isCollectionNillable());
        this.prop = prop;

        List<? extends RuntimeTypeRef> types = prop.getTypes();

        Name n = null;

        for (RuntimeTypeRef typeRef : types) {
            Class type = (Class)typeRef.getTarget().getType();
            if(type.isPrimitive())
                type = RuntimeUtil.primitiveToBox.get(type);

            JaxBeanInfo beanInfo = grammar.getOrCreate(typeRef.getTarget());
            TagAndType tt = new TagAndType(
                                grammar.nameBuilder.createElementName(typeRef.getTagName()),
                                beanInfo);
            typeMap.put(type,tt);
            refs.put(typeRef,beanInfo);
            if(typeRef.isNillable() && n==null)
                n = tt.tagName;
        }

        nillableTagName = n;
    }

    @Override
    public void wrapUp() {
        super.wrapUp();
        refs = null;
        prop = null;    // avoid keeping model objects live
    }

    protected void serializeListBody(BeanT beanT, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException, AccessorException {
        ListIterator<ItemT> itr = lister.iterator(list, w);

        boolean isIdref = itr instanceof Lister.IDREFSIterator; // UGLY

        while(itr.hasNext()) {
            try {
                ItemT item = itr.next();
                if (item != null) {
                    Class itemType = item.getClass();
                    if(isIdref)
                        // This should be the only place where we need to be aware
                        // that the iterator is iterating IDREFS.
                        itemType = ((Lister.IDREFSIterator)itr).last().getClass();

                    // normally, this returns non-null
                    TagAndType tt = typeMap.get(itemType);
                    while(tt==null && itemType!=null) {
                        // otherwise we'll just have to try the slow way
                        itemType = itemType.getSuperclass();
                        tt = typeMap.get(itemType);
                    }

                    if(tt==null) {
                        // item is not of the expected type.
//                        w.reportError(new ValidationEventImpl(ValidationEvent.ERROR,
//                            Messages.UNEXPECTED_JAVA_TYPE.format(
//                                item.getClass().getName(),
//                                getExpectedClassNameList()
//                            ),
//                            w.getCurrentLocation(fieldName)));
//                        continue;

                        // see the similar code in SingleElementNodeProperty.
                        // for the purpose of simple type substitution, make it a non-error

                        w.startElement(typeMap.values().iterator().next().tagName,null);
                        w.childAsXsiType(item,fieldName,w.grammar.getBeanInfo(Object.class), false);
                    } else {
                        w.startElement(tt.tagName,null);
                        serializeItem(tt.beanInfo,item,w);
                    }

                    w.endElement();
                } else {
                    if(nillableTagName!=null) {
                        w.startElement(nillableTagName,null);
                        w.writeXsiNilTrue();
                        w.endElement();
                    }
                }
            } catch (JAXBException e) {
                w.reportError(fieldName,e);
                // recover by ignoring this item
            }
        }
    }

    /**
     * Serializes one item of the property.
     */
    protected abstract void serializeItem(JaxBeanInfo expected, ItemT item, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException;


    public void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<ChildLoader> loaders) {

        // all items go to the same lister,
        // so they should share the same offset.
        int offset = chain.allocateOffset();
        Receiver recv = new ReceiverImpl(offset);

        for (RuntimeTypeRef typeRef : prop.getTypes()) {

            Name tagName = chain.context.nameBuilder.createElementName(typeRef.getTagName());
            Loader item = createItemUnmarshaller(chain,typeRef);

            if(typeRef.isNillable() || chain.context.allNillable)
                item = new XsiNilLoader.Array(item);
            if(typeRef.getDefaultValue()!=null)
                item = new DefaultValueLoaderDecorator(item,typeRef.getDefaultValue());

            loaders.put(tagName,new ChildLoader(item,recv));
        }
    }

    public final PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    /**
     * Creates a loader handler that unmarshals the body of the item.
     *
     * <p>
     * This will be sandwiched into <item> ... </item>.
     *
     * <p>
     * When unmarshalling the body of item, the Pack of {@link Lister} is available
     * as the handler state.
     *
     * @param chain
     * @param typeRef
     */
    private Loader createItemUnmarshaller(UnmarshallerChain chain, RuntimeTypeRef typeRef) {
        if(PropertyFactory.isLeaf(typeRef.getSource())) {
            final Transducer xducer = typeRef.getTransducer();
            return new TextLoader(xducer);
        } else {
            return refs.get(typeRef).getLoader(chain.context,true);
        }
    }

    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if(wrapperTagName!=null) {
            if(wrapperTagName.equals(nsUri,localName))
                return acc;
        } else {
            for (TagAndType tt : typeMap.values()) {
                if(tt.tagName.equals(nsUri,localName))
                    // when we can't distinguish null and empty list, JAX-WS doesn't want to see
                    // null (just like any user apps), but since we are providing a raw accessor,
                    // which just grabs the value from the field, we wrap it so that it won't return
                    // null.
                    return new NullSafeAccessor<BeanT,ListT,Object>(acc,lister);
            }
        }
        return null;
    }
}
