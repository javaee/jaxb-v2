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
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
public class CompositeStructureBeanInfo extends JaxBeanInfo<CompositeStructure> {
    public CompositeStructureBeanInfo(JAXBContextImpl context) {
        super(context,null, CompositeStructure.class,false,true,false);
    }

    public String getElementNamespaceURI(CompositeStructure o) {
        throw new UnsupportedOperationException();
    }

    public String getElementLocalName(CompositeStructure o) {
        throw new UnsupportedOperationException();
    }

    public CompositeStructure createInstance(UnmarshallingContext context) throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException {
        throw new UnsupportedOperationException();
    }

    public boolean reset(CompositeStructure o, UnmarshallingContext context) throws SAXException {
        throw new UnsupportedOperationException();
    }

    public String getId(CompositeStructure o, XMLSerializer target) throws SAXException {
        return null;
    }

    public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        // no unmarshaller support for this.
        throw new UnsupportedOperationException();
    }

    public void serializeRoot(CompositeStructure o, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        target.reportError(
                new ValidationEventImpl(
                        ValidationEvent.ERROR,
                        Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(o.getClass().getName()),
                        null,
                        null));
    }

    public void serializeURIs(CompositeStructure o, XMLSerializer target) throws SAXException {
        // noop
    }

    public void serializeAttributes(CompositeStructure o, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        // noop
    }

    public void serializeBody(CompositeStructure o, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        int len = o.bridges.length;
        for( int i=0; i<len; i++ ) {
            Object value = o.values[i];
            InternalBridge bi = (InternalBridge)o.bridges[i];
            bi.marshal( value, target );
        }
    }

    public Transducer<CompositeStructure> getTransducer() {
        return null;
    }
}
