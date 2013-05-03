/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.model;

import javax.activation.MimeType;

import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.xsom.XmlString;

/**
 * Information about how another type is referenced.
 *
 * <p>
 * In practice it is often easier to use {@link CTypeInfo}
 * instead of {@link NType}, so this interface defines {@link #getInfo()}.
 *
 * @author Kohsuke Kawaguchi
 * @see TypeUseImpl
 */
public interface TypeUse {
    /**
     * If the use can hold multiple values of the specified type.
     */
    boolean isCollection();

    /**
     * If this type use is adapting the type, returns the adapter.
     * Otherwise return null.
     */
    CAdapter getAdapterUse();

    /**
     * Individual item type.
     */
    CNonElement getInfo();

    /**
     * Whether the referenced type (individual item type in case of collection)
     * is ID/IDREF.
     *
     * <p>
     * ID is a property of a relationship. When a bean Foo has an ID property
     * called 'bar' whose type is String, Foo isn't an ID, String isn't an ID,
     * but this relationship is an ID (in the sense that Foo uses this String
     * as an ID.)
     *
     * <p>
     * The same thing can be said with IDREF. When Foo refers to Bar by means of
     * IDREF, neither Foo nor Bar is IDREF.
     *
     * <p>
     * That's why we have this method in {@link TypeUse}.
     */
    ID idUse();

    /**
     * A {@link TypeUse} can have an associated MIME type.
     */
    MimeType getExpectedMimeType();

    /**
     * Creates a constant for the given lexical value.
     *
     * <p>
     * For example, to create a constant 1 for <tt>xs:int</tt>, you'd do:
     * <pre>
     * CBuiltinLeafInfo.INT.createConstant( codeModel, "1", null );
     * </pre>
     *
     * <p>
     * This method is invoked at the backend as a part of the code generation process.
     *
     * @throws IllegalStateException
     *      if the type isn't bound to a text in XML.
     *
     * @return null
     *      if the constant cannot be created for this {@link TypeUse}
     *      (such as when it's a collection)
     */
    JExpression createConstant(Outline outline, XmlString lexical);
}
