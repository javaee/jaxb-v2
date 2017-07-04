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
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.Scope;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;

import org.xml.sax.SAXException;

/**
 * Commonality between {@link ArrayElementProperty} and {@link ArrayReferenceNodeProperty}.
 *
 * Mostly handles the unmarshalling of the wrapper element.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ArrayERProperty<BeanT,ListT,ItemT> extends ArrayProperty<BeanT,ListT,ItemT> {

    /**
     * Wrapper tag name if any, or null.
     */
    protected final Name wrapperTagName;

    /**
     * True if the wrapper tag name is nillable.
     * Always false if {@link #wrapperTagName}==null.
     */
    protected final boolean isWrapperNillable;

    protected ArrayERProperty(JAXBContextImpl grammar, RuntimePropertyInfo prop, QName tagName, boolean isWrapperNillable) {
        super(grammar,prop);
        if(tagName==null)
            this.wrapperTagName = null;
        else
            this.wrapperTagName = grammar.nameBuilder.createElementName(tagName);
        this.isWrapperNillable = isWrapperNillable;
    }

    /**
     * Used to handle the collection wrapper element.
     */
    private static final class ItemsLoader extends Loader {

        private final Accessor acc;
        private final Lister lister;

        public ItemsLoader(Accessor acc, Lister lister, QNameMap<ChildLoader> children) {
            super(false);
            this.acc = acc;
            this.lister = lister;
            this.children = children;
        }

        @Override
        public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            UnmarshallingContext context = state.getContext();
            context.startScope(1);
            // inherit the target so that our children can access its target
            state.setTarget(state.getPrev().getTarget());

            // start it now, so that even if there's no children we can still return empty collection
            context.getScope(0).start(acc,lister);
        }

        private final QNameMap<ChildLoader> children;

        @Override
        public void childElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            ChildLoader child = children.get(ea.uri,ea.local);
            if (child == null) {
                child = children.get(CATCH_ALL);
            }
            if (child == null) {
                super.childElement(state,ea);
                return;
            }
            state.setLoader(child.loader);
            state.setReceiver(child.receiver);
        }

        @Override
        public void leaveElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            state.getContext().endScope(1);
        }

        @Override
        public Collection<QName> getExpectedChildElements() {
            return children.keySet();
        }
    }

    public final void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ListT list = acc.get(o);

        if(list!=null) {
            if(wrapperTagName!=null) {
                w.startElement(wrapperTagName,null);
                w.endNamespaceDecls(list);
                w.endAttributes();
            }

            serializeListBody(o,w,list);

            if(wrapperTagName!=null)
                w.endElement();
        } else {
            // list is null
            if(isWrapperNillable) {
                w.startElement(wrapperTagName,null);
                w.writeXsiNilTrue();
                w.endElement();
            } // otherwise don't print the wrapper tag name
        }
    }

    /**
     * Serializes the items of the list.
     * This method is invoked after the necessary wrapper tag is produced (if necessary.)
     *
     * @param list
     *      always non-null.
     */
    protected abstract void serializeListBody(BeanT o, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException, AccessorException;

    /**
     * Creates the unmarshaler to unmarshal the body.
     */
    protected abstract void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<ChildLoader> loaders);


    public final void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> loaders) {
        if(wrapperTagName!=null) {
            UnmarshallerChain c = new UnmarshallerChain(chain.context);
            QNameMap<ChildLoader> m = new QNameMap<ChildLoader>();
            createBodyUnmarshaller(c,m);
            Loader loader = new ItemsLoader(acc, lister, m);
            if(isWrapperNillable || chain.context.allNillable)
                loader = new XsiNilLoader(loader);
            loaders.put(wrapperTagName,new ChildLoader(loader,null));
        } else {
            createBodyUnmarshaller(chain,loaders);
        }
    }

    /**
     * {@link Receiver} that puts the child object into the {@link Scope} object.
     */
    protected final class ReceiverImpl implements Receiver {
        private final int offset;

        protected ReceiverImpl(int offset) {
            this.offset = offset;
        }

        public void receive(UnmarshallingContext.State state, Object o) throws SAXException {
            state.getContext().getScope(offset).add(acc,lister,o);
        }
    }}
