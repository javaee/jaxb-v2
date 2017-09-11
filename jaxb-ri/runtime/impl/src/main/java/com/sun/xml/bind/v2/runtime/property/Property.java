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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

import org.xml.sax.SAXException;

/**
 * A JAXB property that constitutes a JAXB-bound bean.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Property<BeanT> extends StructureLoaderBuilder {

//    // is this method necessary? --> probably not
//    RuntimePropertyInfo owner();

    /**
     * Resets the property value on the given object.
     *
     * <p>
     * ... for example by setting 0 or null.
     */
    void reset( BeanT o ) throws AccessorException;

    /**
     * @see JaxBeanInfo#serializeBody(Object, XMLSerializer)
     *
     * @param outerPeer
     *      used when this property is expected to print out an element
     *      and that should be associated with this outer peer. normally null.
     *      this is only used for {@link JaxBeanInfo} for {@link JAXBElement}s.
     * @throws AccessorException
     *      If thrown, caught by the caller and reported.
     */
    public void serializeBody(BeanT beanT, XMLSerializer target, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException;

    /**
     * @see JaxBeanInfo#serializeURIs(Object, XMLSerializer)
     */
    public void serializeURIs(BeanT beanT, XMLSerializer target) throws SAXException, AccessorException;

    /**
     * Returns true if
     * {@link #serializeURIs(Object,XMLSerializer)} performs some meaningful action.
     */
    public boolean hasSerializeURIAction();

//    /**
//     * Builds the unmarshaller.
//     *
//     * @param grammar
//     *      the context object to which this property ultimately belongs to.
//     *      a property will only belong to one grammar, but to reduce the memory footprint
//     *      we don't keep that information stored in {@link Property}, and instead we
//     *      just pass the value as a parameter when needed.
//     */
//    Unmarshaller.Handler createUnmarshallerHandler(JAXBContextImpl grammar, Unmarshaller.Handler tail);

    /**
     * Gets the value of the property.
     *
     * This method is only used when the corresponding {@link PropertyInfo#id()} is {@link ID#ID},
     * and therefore the return type is fixed to {@link String}.
     */
    String getIdValue(BeanT bean) throws AccessorException, SAXException;

    /**
     * Gets the Kind of property
     * @return
     *      always non-null.
     */
    PropertyKind getKind();


    // UGLY HACK to support JAX-WS
    // if other clients want to access those functionalities,
    // we should design a better model
    /**
     * If this property is mapped to the specified element,
     * return an accessor to it.
     *
     * @return
     *      null if the property is not mapped to the specified element.
     */
    Accessor getElementPropertyAccessor(String nsUri,String localName);

    /**
     * Called at the end of the {@link JAXBContext} initialization phase
     * to clean up any unnecessary references.
     */
    void wrapUp();

    /**
     * Provides more {@link RuntimePropertyInfo} information on the property.
     *
     * @return
     *      null if RETAIN_REFERENCE_TO_INFO property is not set on the {@link JAXBContext}
     */
    public RuntimePropertyInfo getInfo();
    
    public boolean isHiddenByOverride();
    
    public void setHiddenByOverride(boolean hidden);
    
    public String getFieldName();    
}
