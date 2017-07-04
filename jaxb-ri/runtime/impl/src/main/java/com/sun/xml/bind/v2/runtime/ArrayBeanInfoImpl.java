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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} implementation that binds T[] to a complex type
 * with an element for each item.
 *
 * @author Kohsuke Kawaguchi
 */
final class ArrayBeanInfoImpl  extends JaxBeanInfo {

    private final Class itemType;
    private final JaxBeanInfo itemBeanInfo;
    private Loader loader;

    public ArrayBeanInfoImpl(JAXBContextImpl owner, RuntimeArrayInfo rai) {
        super(owner,rai,rai.getType(), rai.getTypeName(), false, true, false);
        this.itemType = jaxbType.getComponentType();
        this.itemBeanInfo = owner.getOrCreate(rai.getItemType());
    }

    @Override
    protected void link(JAXBContextImpl grammar) {
        getLoader(grammar,false);
        super.link(grammar);
    }

    private final class ArrayLoader extends Loader implements Receiver {
        public ArrayLoader(JAXBContextImpl owner) {
            super(false);
            itemLoader = itemBeanInfo.getLoader(owner,true);
        }

        private final Loader itemLoader;

        @Override
        public void startElement(UnmarshallingContext.State state, TagName ea) {
            state.setTarget(new ArrayList());
        }

        @Override
        public void leaveElement(UnmarshallingContext.State state, TagName ea) {
            state.setTarget(toArray((List)state.getTarget()));
        }

        @Override
        public void childElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
            if(ea.matches("","item")) {
                state.setLoader(itemLoader);
                state.setReceiver(this);
            } else {
                super.childElement(state,ea);
            }
        }

        @Override
        public Collection<QName> getExpectedChildElements() {
            return Collections.singleton(new QName("","item"));
        }

        public void receive(UnmarshallingContext.State state, Object o) {
            ((List)state.getTarget()).add(o);
        }
    }

    protected Object toArray( List list ) {
        int len = list.size();
        Object array = Array.newInstance(itemType,len);
        for( int i=0; i<len; i++ )
            Array.set(array,i,list.get(i));
        return array;
    }

    public void serializeBody(Object array, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        int len = Array.getLength(array);
        for( int i=0; i<len; i++ )  {
            Object item = Array.get(array,i);
            // TODO: check the namespace URI.
            target.startElement("","item",null,null);
            if(item==null) {
                target.writeXsiNilTrue();
            } else {
                target.childAsXsiType(item,"arrayItem",itemBeanInfo, false);
            }
            target.endElement();
        }
    }

    public final String getElementNamespaceURI(Object array) {
        throw new UnsupportedOperationException();
    }

    public final String getElementLocalName(Object array) {
        throw new UnsupportedOperationException();
    }

    public final Object createInstance(UnmarshallingContext context) {
        // we first create a List and then later convert it to an array
        return new ArrayList();
    }

    public final boolean reset(Object array, UnmarshallingContext context) {
        return false;
    }

    public final String getId(Object array, XMLSerializer target) {
        return null;
    }

    public final void serializeAttributes(Object array, XMLSerializer target) {
        // noop
    }

    public final void serializeRoot(Object array, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        target.reportError(
                new ValidationEventImpl(
                        ValidationEvent.ERROR,
                        Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(array.getClass().getName()),
                        null,
                        null));
    }

    public final void serializeURIs(Object array, XMLSerializer target) {
        // noop
    }

    public final Transducer getTransducer() {
        return null;
    }

    public final Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        if(loader==null)
            loader = new ArrayLoader(context);

        // type substitution not possible
        return loader;
    }
}
