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

package com.sun.xml.bind.v2.runtime.unmarshaller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.property.StructureLoaderBuilder;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.util.QNameMap;

import java.util.Iterator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Loads children of an element.
 *
 * <p>
 * This loader works with a single {@link JaxBeanInfo} and handles
 * attributes, child elements, or child text.
 *
 * @author Kohsuke Kawaguchi
 */
public final class StructureLoader extends Loader {
    /**
     * This map statically stores information of the
     * unmarshaller loader and can be used while unmarshalling
     * Since creating new QNames is expensive use this optimized
     * version of the map
     */
    private final QNameMap<ChildLoader> childUnmarshallers = new QNameMap<ChildLoader>();

    /**
     * Loader that processes elements that didn't match anf of the {@link #childUnmarshallers}.
     * Can be null.
     */
    private /*final*/ ChildLoader catchAll;

    /**
     * If we have a loader for processing text. Otherwise null.
     */
    private /*final*/ ChildLoader textHandler;

    /**
     * Unmarshallers for attribute values.
     * May be null if no attribute is expected and {@link #attCatchAll}==null.
     */
    private /*final*/ QNameMap<TransducedAccessor> attUnmarshallers;

    /**
     * This will receive all the attributes
     * that were not processed. Never be null.
     */
    private /*final*/ Accessor<Object,Map<QName,String>> attCatchAll;

    private final JaxBeanInfo beanInfo;

    /**
     * The number of scopes this dispatcher needs to keep active.
     */
    private /*final*/ int frameSize;

    // this class is potentially useful for general audience, not just for ClassBeanInfoImpl,
    // but since right now that is the only user, we make the construction code very specific
    // to ClassBeanInfoImpl. See rev.1.5 of this file for the original general purpose definition.
    public StructureLoader(ClassBeanInfoImpl beanInfo) {
        super(true);
        this.beanInfo = beanInfo;
    }

    /**
     * Completes the initialization.
     *
     * <p>
     * To fix the cyclic reference issue, the main part of the initialization needs to be done
     * after a {@link StructureLoader} is set to {@link ClassBeanInfoImpl#loader}.
     */
    public void init( JAXBContextImpl context, ClassBeanInfoImpl beanInfo, Accessor<?,Map<QName,String>> attWildcard) {
        UnmarshallerChain chain = new UnmarshallerChain(context);
        for (ClassBeanInfoImpl bi = beanInfo; bi != null; bi = bi.superClazz) {
            for (int i = bi.properties.length - 1; i >= 0; i--) {
                Property p = bi.properties[i];

                switch(p.getKind()) {
                case ATTRIBUTE:
                    if(attUnmarshallers==null)
                        attUnmarshallers = new QNameMap<TransducedAccessor>();
                    AttributeProperty ap = (AttributeProperty) p;
                    attUnmarshallers.put(ap.attName.toQName(),ap.xacc);
                    break;
                case ELEMENT:
                case REFERENCE:
                case MAP:
                case VALUE:
                    p.buildChildElementUnmarshallers(chain,childUnmarshallers);
                    break;
                }
            }
        }

        this.frameSize = chain.getScopeSize();

        textHandler = childUnmarshallers.get(StructureLoaderBuilder.TEXT_HANDLER);
        catchAll = childUnmarshallers.get(StructureLoaderBuilder.CATCH_ALL);

        if(attWildcard!=null) {
            attCatchAll = (Accessor<Object,Map<QName,String>>) attWildcard;
            // we use attUnmarshallers==null as a sign to skip the attribute processing
            // altogether, so if we have an att wildcard we need to have an empty qname map.
            if(attUnmarshallers==null)
                attUnmarshallers = EMPTY;
        } else {
            attCatchAll = null;
        }
    }

    @Override
    public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        UnmarshallingContext context = state.getContext();

        // create the object to unmarshal
        Object child;
        assert !beanInfo.isImmutable();

        // let's see if we can reuse the existing peer object
        child = context.getInnerPeer();

        if(child != null && beanInfo.jaxbType!=child.getClass()) 
            child = null;   // unexpected type.

        if(child != null) 
            beanInfo.reset(child,context);

        if(child == null)
            child = context.createInstance(beanInfo);

        context.recordInnerPeer(child);

        state.setTarget(child);

        fireBeforeUnmarshal(beanInfo, child, state);


        context.startScope(frameSize);

        if(attUnmarshallers!=null) {
            Attributes atts = ea.atts;
            for (int i = 0; i < atts.getLength(); i ++){
                String auri = atts.getURI(i);
                // may be empty string based on parser settings
                String alocal = atts.getLocalName(i);
                if ("".equals(alocal)) {
                    alocal = atts.getQName(i);
                }
                String avalue = atts.getValue(i);                
                TransducedAccessor xacc = attUnmarshallers.get(auri, alocal);
                try {
                    if(xacc!=null) {
                        xacc.parse(child,avalue);
                    } else if (attCatchAll!=null) {
                        String qname = atts.getQName(i);
                        if(atts.getURI(i).equals(WellKnownNamespace.XML_SCHEMA_INSTANCE))
                            continue;   // xsi:* attributes are meant to be processed by us, not by user apps.
                        Object o = state.getTarget();
                        Map<QName,String> map = attCatchAll.get(o);
                        if(map==null) {
                            // TODO: use  ClassFactory.inferImplClass(sig,knownImplClasses)

                            // if null, create a new map.
                            if(attCatchAll.valueType.isAssignableFrom(HashMap.class))
                                map = new HashMap<QName,String>();
                            else {
                                // we don't know how to create a map for this.
                                // report an error and back out
                                context.handleError(Messages.UNABLE_TO_CREATE_MAP.format(attCatchAll.valueType));
                                return;
                            }
                            attCatchAll.set(o,map);
                        }

                        String prefix;
                        int idx = qname.indexOf(':');
                        if(idx<0)   prefix="";
                        else        prefix=qname.substring(0,idx);

                        map.put(new QName(auri,alocal,prefix),avalue);
                    }
                } catch (AccessorException e) {
                   handleGenericException(e,true);
                }
            }
        }
    }

    @Override
    public void childElement(UnmarshallingContext.State state, TagName arg) throws SAXException {
        ChildLoader child = childUnmarshallers.get(arg.uri,arg.local);
        if(child == null) {
            Boolean backupWithParentNamespace = ((JAXBContextImpl) state.getContext().getJAXBContext()).backupWithParentNamespace;
			backupWithParentNamespace = backupWithParentNamespace != null
					? backupWithParentNamespace
					: Boolean.parseBoolean(Util.getSystemProperty(JAXBRIContext.BACKUP_WITH_PARENT_NAMESPACE));
            if ((beanInfo != null) && (beanInfo.getTypeNames() != null) && backupWithParentNamespace) {
                Iterator<?> typeNamesIt = beanInfo.getTypeNames().iterator();
                QName parentQName = null;
                if ((typeNamesIt != null) && (typeNamesIt.hasNext()) && (catchAll == null)) {
                    parentQName = (QName) typeNamesIt.next();
                    String parentUri = parentQName.getNamespaceURI();
                    child = childUnmarshallers.get(parentUri, arg.local);
                }
            }
            if (child == null) {
                child = catchAll;
                if(child==null) {
                    super.childElement(state,arg);
                    return;
                }
            }                    
        }

        state.setLoader(child.loader);
        state.setReceiver(child.receiver);
    }

    @Override
    public Collection<QName> getExpectedChildElements() {
        return childUnmarshallers.keySet();
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        return attUnmarshallers.keySet();
    }

    @Override
    public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
        if(textHandler!=null)
            textHandler.loader.text(state,text);
    }

    @Override
    public void leaveElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        state.getContext().endScope(frameSize);
        fireAfterUnmarshal(beanInfo, state.getTarget(), state.getPrev());
    }

    private static final QNameMap<TransducedAccessor> EMPTY = new QNameMap<TransducedAccessor>();

    public JaxBeanInfo getBeanInfo() {
        return beanInfo;
    }
}
