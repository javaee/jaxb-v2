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

import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;

import org.xml.sax.SAXException;


/**
 * Responsible for converting a Java object to a lexical representation
 * and vice versa.
 *
 * <p>
 * An implementation of this interface hides how this conversion happens.
 *
 * <p>
 * {@link Transducer}s are immutable.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface Transducer<ValueT> {

    /**
     * If true, this {@link Transducer} doesn't declare any namespace,
     * and therefore {@link #declareNamespace(Object, XMLSerializer)} is no-op.
     *
     * It also means that the {@link #parse(CharSequence)} method
     * won't use the context parameter.
     */
    boolean useNamespace();

    /**
     * Declares the namespace URIs used in the given value to {@code w}.
     *
     * @param o
     *      never be null.
     * @param w
     *      may be null if {@code !{@link #useNamespace()}}.
     */
    void declareNamespace( ValueT o, XMLSerializer w ) throws AccessorException;

    /**
     * Converts the given value to its lexical representation.
     *
     * @param o
     *      never be null.
     * @return
     *      always non-null valid lexical representation.
     */
    @NotNull CharSequence print(@NotNull ValueT o) throws AccessorException;

    /**
     * Converts the lexical representation to a value object.
     *
     * @param lexical
     *      never be null.
     * @throws AccessorException
     *      if the transducer is used to parse an user bean that uses {@link XmlValue},
     *      then this exception may occur when it tries to set the leaf value to the bean.
     * @throws SAXException
     *      if the lexical form is incorrect, the error should be reported
     *      and SAXException may thrown (or it can return null to recover.)
     */
    ValueT parse(CharSequence lexical) throws AccessorException, SAXException;

    /**
     * Sends the result of the {@link #print(Object)} operation
     * to one of the {@link XMLSerializer#text(String, String)} method,
     * but with the best representation of the value, not necessarily String.
     */
    void writeText(XMLSerializer w, ValueT o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException;

    /**
     * Sends the result of the {@link #print(Object)} operation
     * to one of the {@link XMLSerializer#leafElement(Name, String, String)} method.
     * but with the best representation of the value, not necessarily String.
     */
    void writeLeafElement(XMLSerializer w, Name tagName, @NotNull ValueT o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException;

    /**
     * Transducers implicitly work against a single XML type,
     * but sometimes (most notably {@link XMLGregorianCalendar},
     * an instance may choose different XML types.
     *
     * @return
     *      return non-null from this method allows transducers
     *      to specify the type it wants to marshal to.
     *      Most of the time this method returns null, in which case
     *      the implicitly associated type will be used.
     */
    QName getTypeName(@NotNull ValueT instance);
}
