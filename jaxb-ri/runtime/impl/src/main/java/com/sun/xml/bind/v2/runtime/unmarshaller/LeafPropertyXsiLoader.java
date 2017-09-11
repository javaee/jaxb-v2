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

import javax.xml.namespace.QName;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class LeafPropertyXsiLoader extends Loader {

    private final Loader defaultLoader;
    private final TransducedAccessor xacc;
    private final Accessor acc;

    public LeafPropertyXsiLoader(Loader defaultLoader, TransducedAccessor xacc, Accessor acc) {
        this.defaultLoader = defaultLoader;
        this.expectText = true;
        this.xacc = xacc;
        this.acc = acc;
    }


    @Override
    public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        final Loader loader = selectLoader(state, ea);
        state.setLoader(loader);
        loader.startElement(state, ea);
    }

    protected Loader selectLoader(UnmarshallingContext.State state, TagName ea) throws SAXException {

        UnmarshallingContext context = state.getContext();
        JaxBeanInfo beanInfo = null;

        // look for @xsi:type
        Attributes atts = ea.atts;
        int idx = atts.getIndex(WellKnownNamespace.XML_SCHEMA_INSTANCE, "type");

        if (idx >= 0) {
            String value = atts.getValue(idx);

            QName type = DatatypeConverterImpl._parseQName(value, context);

            if (type == null)
                return defaultLoader;

            beanInfo = context.getJAXBContext().getGlobalType(type);
            if (beanInfo == null)
                return defaultLoader;
            ClassBeanInfoImpl cbii;
            try {
                cbii = (ClassBeanInfoImpl) beanInfo;
            } catch (ClassCastException cce) {
                return defaultLoader;
            }

            if (null == cbii.getTransducer()) {
                return defaultLoader;
            }

            return new LeafPropertyLoader(
                    new TransducedAccessor.CompositeTransducedAccessorImpl(
                            state.getContext().getJAXBContext(),
                            cbii.getTransducer(),
                            acc));
        }

        return defaultLoader;
    }

    @Override
    public Collection<QName> getExpectedChildElements() {
        return defaultLoader.getExpectedChildElements();
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        return defaultLoader.getExpectedAttributes();
    }
}
