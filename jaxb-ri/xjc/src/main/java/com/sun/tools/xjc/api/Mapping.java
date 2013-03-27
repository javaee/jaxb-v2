/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.xjc.api;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * JAXB-induced mapping between a Java class
 * and an XML element declaration. A part of the compiler artifacts.
 *
 * <p>
 * To be precise, this is a mapping between two Java classes and an
 * XML element declaration. There's one Java class/interface that
 * represents the element, and there's another Java class/interface that
 * represents the type of the element.
 *
 * The former is called "element representation" and the latter is called
 * "type representation".
 *
 * <p>
 * The {@link Mapping} interface provides operation that lets the caller
 * convert an instance of the element representation to that of the
 * type representation or vice versa.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface Mapping {
    /**
     * Name of the XML element.
     *
     * @return
     *      never be null.
     */
    QName getElement();

    /**
     * Returns the fully-qualified name of the java class for the type of this element.
     *
     * TODO: does this method returns the name of the wrapper bean when it's qualified
     * for the wrapper style? Seems no (consider &lt;xs:element name='foo' type='xs:long' />),
     * but then how does JAX-RPC captures that bean?
     *
     * @return
     *      never be null.
     */
    TypeAndAnnotation getType();

    /**
     * If this element is a so-called "wrapper-style" element,
     * obtains its member information.
     *
     * <p>
     * The notion of the wrapper style should be defined by the JAXB spec,
     * and ideally it should differ from that of the JAX-RPC only at
     * the point where the JAX-RPC imposes additional restriction
     * on the element name.
     *
     * <p>
     * As of this writing the JAXB spec doesn't define "the wrapper style"
     * and as such the exact definition of what XJC thinks
     * "the wrapper style" isn't spec-ed.
     *
     * <p>
     * Ths returned list includes {@link Property} defined not just
     * in this class but in all its base classes.
     *
     * @return
     *      null if this isn't a wrapper-style element.
     *      Otherwise list of {@link Property}s. The order signifies
     *      the order they appeared inside a schema.
     */
    List<? extends Property> getWrapperStyleDrilldown();
}
