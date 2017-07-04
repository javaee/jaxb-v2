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

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TextLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;

import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} implementation for immutable leaf classes.
 *
 * <p>
 * Leaf classes are always bound to a text and they are often immutable.
 * The JAXB spec allows this binding for a few special Java classes plus
 * type-safe enums.
 *
 * <p>
 * This implementation obtains necessary information from {@link RuntimeLeafInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
final class LeafBeanInfoImpl<BeanT> extends JaxBeanInfo<BeanT> {

    private final Loader loader;
    private final Loader loaderWithSubst;

    private final Transducer<BeanT> xducer;

    /**
     * Non-null only if the leaf is also an element.
     */
    private final Name tagName;

    public LeafBeanInfoImpl(JAXBContextImpl grammar, RuntimeLeafInfo li) {
        super(grammar,li,li.getClazz(),li.getTypeNames(),li.isElement(),true,false);

        xducer = li.getTransducer();
        loader = new TextLoader(xducer);
        loaderWithSubst = new XsiTypeLoader(this);

        if(isElement())
            tagName = grammar.nameBuilder.createElementName(li.getElementName());
        else
            tagName = null;
    }

    @Override
    public QName getTypeName(BeanT instance) {
        QName tn = xducer.getTypeName(instance);
        if(tn!=null)    return tn;
        // rely on default
        return super.getTypeName(instance);
    }

    public final String getElementNamespaceURI(BeanT t) {
        return tagName.nsUri;
    }

    public final String getElementLocalName(BeanT t) {
        return tagName.localName;
    }

    public BeanT createInstance(UnmarshallingContext context) {
        throw new UnsupportedOperationException();
    }

    public final boolean reset(BeanT bean, UnmarshallingContext context) {
        return false;
    }

    public final String getId(BeanT bean, XMLSerializer target) {
        return null;
    }

    public final void serializeBody(BeanT bean, XMLSerializer w) throws SAXException, IOException, XMLStreamException {
        // most of the times leaves are printed as leaf element/attribute property,
        // so this code is only used for example when you have multiple XmlElement on a property
        // and some of them are leaves. Hence this doesn't need to be super-fast.
        try {
            xducer.writeText(w,bean,null);
        } catch (AccessorException e) {
            w.reportError(null,e);
        }
    }

    public final void serializeAttributes(BeanT bean, XMLSerializer target) {
        // noop
    }

    public final void serializeRoot(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        if(tagName==null) {
            target.reportError(
                new ValidationEventImpl(
                    ValidationEvent.ERROR,
                    Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(bean.getClass().getName()),
                    null,
                    null));
        }
        else {
            target.startElement(tagName,bean);
            target.childAsSoleContent(bean,null);
            target.endElement();
        }
    }

    public final void serializeURIs(BeanT bean, XMLSerializer target) throws SAXException {
        // TODO: maybe we should create another LeafBeanInfoImpl class for
        // context-dependent xducers?
        if(xducer.useNamespace()) {
            try {
                xducer.declareNamespace(bean,target);
            } catch (AccessorException e) {
                target.reportError(null,e);
            }
        }
    }

    public final Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        if(typeSubstitutionCapable)
            return loaderWithSubst;
        else
            return loader;
    }

    public Transducer<BeanT> getTransducer() {
        return xducer;
    }
}
