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

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.ValuePropertyLoader;

/**
 * Component that contributes element unmarshallers into
 * {@link StructureLoader}.
 *
 * TODO: think of a better name.
 *
 * @author Bhakti Mehta
 */
public interface StructureLoaderBuilder {
    /**
     * Every Property class has an implementation of buildChildElementUnmarshallers
     * which will fill in the specified {@link QNameMap} by elements that are expected
     * by this property.
     */
    void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers);

    /**
     * Magic {@link QName} used to store a handler for the text.
     *
     * <p>
     * To support the mixed content model, {@link StructureLoader} can have
     * at most one {@link ValuePropertyLoader} for processing text
     * found amoung elements.
     *
     * This special text handler is put into the {@link QNameMap} parameter
     * of the {@link #buildChildElementUnmarshallers} method by using
     * this magic token as the key.
     */
    public static final QName TEXT_HANDLER = new QName("\u0000","text");

    /**
     * Magic {@link QName} used to store a handler for the rest of the elements.
     *
     * <p>
     * To support the wildcard, {@link StructureLoader} can have
     * at most one {@link Loader} for processing elements
     * that didn't match any of the named elements.
     *
     * This special text handler is put into the {@link QNameMap} parameter
     * of the {@link #buildChildElementUnmarshallers} method by using
     * this magic token as the key.
     */
    public static final QName CATCH_ALL = new QName("\u0000","catchAll");
}
