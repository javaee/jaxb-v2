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
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.stream.XMLStreamException;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

import org.xml.sax.SAXException;

/**
 * {@link JaxBeanInfo} implementation that binds T[] to a list of simple types.
 * 
 * @author Kohsuke Kawaguchi
 */
final class ValueListBeanInfoImpl extends JaxBeanInfo {

    private final Class itemType;
    private final Transducer xducer;    // for items

    public ValueListBeanInfoImpl(JAXBContextImpl owner, Class arrayType) throws JAXBException {
        super(owner, null, arrayType, false, true, false);
        this.itemType = jaxbType.getComponentType();
        this.xducer = owner.getBeanInfo(arrayType.getComponentType(),true).getTransducer();
        assert xducer!=null;
    }

    private final Loader loader = new Loader(true) {
        @Override
        public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
            List<Object> r = new FinalArrayList<Object>();

            int idx = 0;
            int len = text.length();

            while(true) {
                int p = idx;
                while( p<len && !WhiteSpaceProcessor.isWhiteSpace(text.charAt(p)) )
                    p++;

                CharSequence token = text.subSequence(idx,p);
                if (!token.equals(""))
                    try {
                        r.add(xducer.parse(token));
                    } catch (AccessorException e) {
                        handleGenericException(e,true);
                        continue;   // move on to next
                    }

                if(p==len)      break;  // done

                while( p<len && WhiteSpaceProcessor.isWhiteSpace(text.charAt(p)) )
                    p++;
                if(p==len)      break;  // done

                idx = p;
            }

            state.setTarget(toArray(r));
        }
    };

    private Object toArray( List list ) {
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
            try {
                xducer.writeText(target,item,"arrayItem");
            } catch (AccessorException e) {
                target.reportError("arrayItem",e);
            }
        }
    }

    public final void serializeURIs(Object array, XMLSerializer target) throws SAXException {
        if(xducer.useNamespace()) {
            int len = Array.getLength(array);
            for( int i=0; i<len; i++ )  {
                Object item = Array.get(array,i);
                try {
                    xducer.declareNamespace(item,target);
                } catch (AccessorException e) {
                    target.reportError("arrayItem",e);
                }
            }
        }
    }

    public final String getElementNamespaceURI(Object array) {
        throw new UnsupportedOperationException();
    }

    public final String getElementLocalName(Object array) {
        throw new UnsupportedOperationException();
    }

    public final Object createInstance(UnmarshallingContext context) {
        throw new UnsupportedOperationException();
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

    public final void serializeRoot(Object array, XMLSerializer target) throws SAXException {
        target.reportError(
                new ValidationEventImpl(
                        ValidationEvent.ERROR,
                        Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(array.getClass().getName()),
                        null,
                        null));
    }

    public final Transducer getTransducer() {
        return null;
    }

    public final Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        // type substitution impossible
        return loader;
    }
}
