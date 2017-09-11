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

package com.sun.xml.bind;

import java.util.concurrent.Callable;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlIDREF;

import org.xml.sax.SAXException;

/**
 * Pluggable ID/IDREF handling layer.
 *
 * <p>
 * <b>THIS INTERFACE IS SUBJECT TO CHANGE WITHOUT NOTICE.</b>
 *
 * <p>
 * This 'interface' can be implemented by applications and specified to
 * {@link Unmarshaller#setProperty(String, Object)} to ovierride the ID/IDREF
 * processing of the JAXB RI like this:
 *
 * <pre>
 * unmarshaller.setProperty(IDResolver.class.getName(),new MyIDResolverImpl());
 * </pre>
 *
 * <h2>Error Handling</h2>
 * <p>
 * This component runs inside the JAXB RI unmarshaller. Therefore, it needs
 * to coordinate with the JAXB RI unmarshaller when it comes to reporting
 * errors. This makes sure that applications see consistent error handling behaviors.
 *
 * <p>
 * When the {@link #startDocument(ValidationEventHandler)} method is invoked,
 * the unmarshaller passes in a {@link ValidationEventHandler} that can be used
 * by this component to report any errors encountered during the ID/IDREF processing.
 *
 * <p>
 * When an error is detected, the error should be first reported to this
 * {@link ValidationEventHandler}. If the error is fatal or the event handler
 * decided to abort, the implementation should throw a {@link SAXException}.
 * This signals the unmarshaller to abort the processing.
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB 2.0 beta
 */
public abstract class IDResolver {

    /**
     * Called when the unmarshalling starts.
     *
     * <p>
     * Since one {@link Unmarshaller} may be used multiple times
     * to unmarshal documents, one {@link IDResolver} may be used multiple times, too.
     *
     * @param eventHandler
     *      Any errors found during the unmarshalling should be reported to this object.
     */
    public void startDocument(ValidationEventHandler eventHandler) throws SAXException {

    }

    /**
     * Called after the unmarshalling completes.
     *
     * <p>
     * This is a good opporunity to reset any internal state of this object,
     * so that it doesn't keep references to other objects unnecessarily.
     */
    public void endDocument() throws SAXException {

    }

    /**
     * Binds the given object to the specified ID.
     *
     * <p>
     * While a document is being unmarshalled, every time
     * an ID value is found, this method is invoked to
     * remember the association between ID and objects.
     * This association is supposed to be used later to resolve
     * IDREFs.
     *
     * <p>
     * This method is invoked right away as soon as a new ID value is found.
     *
     * @param id
     *      The ID value found in the document being unmarshalled.
     *      Always non-null.
     * @param obj
     *      The object being unmarshalled which is going to own the ID.
     *      Always non-null.
     */
    public abstract void bind( String id, Object obj ) throws SAXException;

    /**
     * Obtains the object to be pointed by the IDREF value.
     *
     * <p>
     * While a document is being unmarshalled, every time
     * an IDREF value is found, this method is invoked immediately to
     * obtain the object that the IDREF is pointing to.
     *
     * <p>
     * This method returns a {@link Callable} to support forward-references.
     * When this method returns with a non-null return value,
     * the JAXB RI unmarshaller invokes the {@link Callable#call()} method immediately.
     * If the implementation can find the target object (in which case
     * it was a backward reference), then a non-null object shall be returned,
     * and it is used as the target object.
     *
     * <p>
     * When a forward-reference happens, the {@code call} method
     * should return null. In this case the JAXB RI unmarshaller invokes
     * the {@code call} method again after all the documents are fully unmarshalled.
     * If the {@code call} method still returns null, then the JAXB RI unmarshaller
     * treats it as an error.
     *
     * <p>
     * A {@link Callable} object returned from this method may not throw
     * any exception other than a {@link SAXException} (which means a fatal error.)
     *
     * @param id
     *      The IDREF value found in the document being unmarshalled.
     *      Always non-null.
     * @param targetType
     *      The expected type to which ID resolves to. JAXB infers this
     *      information from the signature of the fields that has {@link XmlIDREF}.
     *      When a property is a collection, this parameter will be the type
     *      of the individual item in the collection.
     * @return
     *      null if the implementation is sure that the parameter combination
     *      will never yield a valid object. Otherwise non-null.
     */
    public abstract Callable<?> resolve( String id, Class targetType ) throws SAXException;
}
