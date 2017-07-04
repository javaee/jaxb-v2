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

import javax.xml.bind.annotation.XmlValue;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.ValuePropertyLoader;
import com.sun.xml.bind.v2.util.QNameMap;

import org.xml.sax.SAXException;

/**
 * {@link Property} implementation for {@link XmlValue} properties.
 *
 * <p>
 * This one works for both leaves and nodes, scalars and arrays.
 *
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
public final class ValueProperty<BeanT> extends PropertyImpl<BeanT> {

    /**
     * Heart of the conversion logic.
     */
    private final TransducedAccessor<BeanT> xacc;
    private final Accessor<BeanT,?> acc;


    public ValueProperty(JAXBContextImpl context, RuntimeValuePropertyInfo prop) {
        super(context,prop);
        xacc = TransducedAccessor.get(context,prop);
        acc = prop.getAccessor();   // we only use this for binder, so don't waste memory by optimizing
    }

    public final void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        if(xacc.hasValue(o))
            xacc.writeText(w,o,fieldName);
    }

    public void serializeURIs(BeanT o, XMLSerializer w) throws SAXException, AccessorException {
        xacc.declareNamespace(o,w);
    }

    public boolean hasSerializeURIAction() {
        return xacc.useNamespace();
    }

    public void buildChildElementUnmarshallers(UnmarshallerChain chainElem, QNameMap<ChildLoader> handlers) {
        handlers.put(StructureLoaderBuilder.TEXT_HANDLER,
                new ChildLoader(new ValuePropertyLoader(xacc),null));
    }

    public PropertyKind getKind() {
        return PropertyKind.VALUE;
    }

    public void reset(BeanT o) throws AccessorException {
        acc.set(o,null);
    }

    public String getIdValue(BeanT bean) throws AccessorException, SAXException {
        return xacc.print(bean).toString();
    }

}
