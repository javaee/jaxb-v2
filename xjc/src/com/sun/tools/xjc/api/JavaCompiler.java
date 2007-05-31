/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
