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

package com.sun.xml.bind.api;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlAnyElement;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

/**
 * Dynamically locates classes to represent elements discovered during the unmarshalling.
 *
 * <p>
 * <b>THIS INTERFACE IS SUBJECT TO CHANGE WITHOUT NOTICE.</b>
 *
 * <h2>Background</h2>
 * <p>
 * {@link JAXBContext#newInstance(Class...)} requires that application informs JAXB
 * about all the classes that it may see in the instance document. While this allows
 * JAXB to take time to optimize the unmarshalling, it is sometimes inconvenient
 * for applications.
 *
 * <p>
 * This is where {@link ClassResolver} comes to resucue.
 *
 * <p>
 * A {@link ClassResolver} instance can be specified on {@link Unmarshaller} via
 * {@link Unmarshaller#setProperty(String, Object)} as follows:
 *
 * <pre>
 * unmarshaller.setProperty( ClassResolver.class.getName(), new MyClassResolverImpl() );
 * </pre>
 *
 * <p>
 * When an {@link Unmarshaller} encounters (i) an unknown root element or (ii) unknown
 * elements where unmarshaller is trying to unmarshal into {@link XmlAnyElement} with
 * {@code lax=true}, unmarshaller calls {@link #resolveElementName(String, String)}
 * method to see if the application may be able to supply a class that corresponds
 * to that class.
 *
 * <p>
 * When a {@link Class} is returned, a new {@link JAXBContext} is created with
 * all the classes known to it so far, plus a new class returned. This operation
 * may fail (for example because of some conflicting annotations.) This failure
 * is handled just like {@link Exception}s thrown from
 * {@link ClassResolver#resolveElementName(String, String)}.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1
 */
public abstract class ClassResolver {
    /**
     * JAXB calls this method when it sees an unknown element.
     *
     * <p>
     * See the class javadoc for details.
     *
     * @param nsUri
     *      Namespace URI of the unknown element. Can be empty but never null.
     * @param localName
     *      Local name of the unknown element. Never be empty nor null.
     *
     * @return
     *      If a non-null class is returned, it will be used to unmarshal this element.
     *      If null is returned, the resolution is assumed to be failed, and
     *      the unmarshaller will behave as if there was no {@link ClassResolver}
     *      to begin with (that is, to report it to {@link ValidationEventHandler},
     *      then move on.)
     *
     * @throws Exception
     *      Throwing any {@link RuntimeException} causes the unmarshaller to stop
     *      immediately. The exception will be propagated up the call stack.
     *      Throwing any other checked {@link Exception} results in the error
     *      reproted to {@link ValidationEventHandler} (just like any other error
     *      during the unmarshalling.)
     */
    public abstract @Nullable Class<?> resolveElementName(@NotNull String nsUri, @NotNull String localName) throws Exception;
}
