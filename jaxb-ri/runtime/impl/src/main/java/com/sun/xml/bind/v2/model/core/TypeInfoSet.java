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

package com.sun.xml.bind.v2.model.core;

import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * Root of models.&nbsp;Set of {@link TypeInfo}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypeInfoSet<T,C,F,M> {

    /**
     * {@link Navigator} for this model.
     */
    Navigator<T,C,F,M> getNavigator();

//  turns out we can't have AnnotationReader in XJC, so it's impossible to have this here.
//  perhaps we should revisit this in the future.
//    /**
//     * {@link AnnotationReader} for this model.
//     */
//    AnnotationReader<T,C,F,M> getReader();

    /**
     * Returns a {@link TypeInfo} for the given type.
     *
     * @return
     *      null if the specified type cannot be bound by JAXB, or
     *      not known to this set.
     */
    NonElement<T,C> getTypeInfo( T type );

    /**
     * Gets the {@link TypeInfo} for the any type.
     */
    NonElement<T,C> getAnyTypeInfo();

    /**
     * Returns a {@link ClassInfo}, {@link ArrayInfo}, or {@link LeafInfo}
     * for the given bean.
     *
     * <p>
     * This method is almost like refinement of {@link #getTypeInfo(Object)} except
     * our C cannot derive from T.
     *
     * @return
     *      null if the specified type is not bound by JAXB or otherwise
     *      unknown to this set.
     */
    NonElement<T,C> getClassInfo( C type );

    /**
     * Returns all the {@link ArrayInfo}s known to this set.
     */
    Map<? extends T,? extends ArrayInfo<T,C>> arrays();

    /**
     * Returns all the {@link ClassInfo}s known to this set.
     */
    Map<C,? extends ClassInfo<T,C>> beans();

    /**
     * Returns all the {@link BuiltinLeafInfo}s known to this set.
     */
    Map<T,? extends BuiltinLeafInfo<T,C>> builtins();

    /**
     * Returns all the {@link EnumLeafInfo}s known to this set.
     */
    Map<C,? extends EnumLeafInfo<T,C>> enums();

    /**
     * Returns a {@link ElementInfo} for the given element.
     *
     * @param scope
     *      if null, return the info about a global element.
     *      Otherwise return a local element in the given scope if available,
     *      then look for a global element next.
     */
    ElementInfo<T,C> getElementInfo( C scope, QName name );

    /**
     * Returns a type information for the given reference.
     */
    NonElement<T,C> getTypeInfo(Ref<T,C> ref);

    /**
     * Returns all  {@link ElementInfo}s in the given scope.
     *
     * @param scope
     *      if non-null, this method only returns the local element mapping.
     */
    Map<QName,? extends ElementInfo<T,C>> getElementMappings( C scope );

    /**
     * Returns all the {@link ElementInfo} known to this set.
     */
    Iterable<? extends ElementInfo<T,C>> getAllElements();


    /**
     * Gets all {@link XmlSchema#xmlns()} found in this context for the given namespace URI.
     *
     * <p>
     * This operation is expected to be only used in schema generator, so it can be slow.
     *  
     * @return
     *      A map from prefixes to namespace URIs, which should be declared when generating a schema.
     *      Could be empty but never null.
     */
    Map<String,String> getXmlNs(String namespaceUri);

    /**
     * Gets {@link XmlSchema#location()} found in this context.
     *
     * <p>
     * This operation is expected to be only used in schema generator, so it can be slow.
     *
     * @return
     *      A map from namespace URI to the value of the location.
     *      If the entry is missing, that means a schema should be generated for that namespace.
     *      If the value is "", that means the schema location is implied
     *      ({@code <xs:schema namespace="..."/>} w/o schemaLocation.)
     */
    Map<String,String> getSchemaLocations();

    /**
     * Gets the reasonable {@link XmlNsForm} for the given namespace URI.
     *
     * <p>
     * The spec doesn't define very precisely what the {@link XmlNsForm} value
     * for the given namespace would be, so this method is implemented in rather
     * ad-hoc way. It should work as what most people expect for simple cases.
     *
     * @return never null.
     */
    XmlNsForm getElementFormDefault(String nsUri);

    /**
     * Gets the reasonable {@link XmlNsForm} for the given namespace URI.
     *
     * <p>
     * The spec doesn't define very precisely what the {@link XmlNsForm} value
     * for the given namespace would be, so this method is implemented in rather
     * ad-hoc way. It should work as what most people expect for simple cases.
     *
     * @return never null.
     */
    XmlNsForm getAttributeFormDefault(String nsUri);

    /**
     * Dumps this model into XML.
     *
     * For debug only.
     *
     * TODO: not sure if this actually works. We don't really know what are T,C.
     */
    public void dump( Result out ) throws JAXBException;
}
