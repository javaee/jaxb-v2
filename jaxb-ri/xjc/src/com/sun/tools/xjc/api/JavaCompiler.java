/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.xjc.api;

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;


/**
 * Java-to-Schema compiler.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface JavaCompiler {
    
    /**
     * Compiles the given annotated Java source code.
     *
     * <p>
     * This operation takes a set of "root types", then compute the list of
     * all the types that need to be bound by forming a transitive reflexive
     * closure of types that are referenced by the root types.
     *
     * <p>
     * Errors will be sent to {@link AnnotationProcessorEnvironment#getMessager()}.
     *
     * @param rootTypes
     *      The list of types that needs to be bound to XML.
     *      "root references" from JAX-RPC to JAXB is always in the form of (type,annotations) pair.
     *
     * @param additionalElementDecls
     *      Add element declarations for the specified element names to
     *      the XML types mapped from the corresponding {@link Reference}s.
     *      Those {@link Reference}s must be included in the <tt>rootTypes</tt> parameter.
     *      In this map, a {@link Reference} can be null, in which case the element name is
     *      declared to have an empty complex type.
     *      (&lt;xs:element name='foo'>&lt;xs:complexType/>&lt;/xs:element>)
     *      This parameter can be null, in which case the method behaves as if the empty map is given.
     *
     * @param defaultNamespaceRemap
     *      If not-null, all the uses of the empty default namespace ("") will
     *      be replaced by this namespace URI.
     *
     * @param source
     *      The caller supplied view to the annotated source code that JAXB is going to process.
     *
     * @return
     *      Non-null if no error was reported. Otherwise null.
     */
    J2SJAXBModel bind(
            Collection<Reference> rootTypes,
            Map<QName, Reference> additionalElementDecls,
            String defaultNamespaceRemap,
            AnnotationProcessorEnvironment source );
}
