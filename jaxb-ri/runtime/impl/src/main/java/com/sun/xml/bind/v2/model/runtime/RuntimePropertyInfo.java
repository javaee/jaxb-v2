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

package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;
import java.util.Collection;

import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * {@link PropertyInfo} that exposes more information.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface RuntimePropertyInfo extends PropertyInfo<Type,Class> {

    /** {@inheritDoc} */
    Collection<? extends RuntimeTypeInfo> ref();


    /**
     * Gets the {@link Accessor} for this property.
     *
     * <p>
     * Even for a multi-value property, this method returns an accessor
     * to that property. IOW, the accessor works against the raw type.
     *
     * <p>
     * This methods returns unoptimized accessor (because optimization
     * accessors are often combined into bigger pieces, and optimization
     * generally works better if you can look at a bigger piece, as opposed
     * to individually optimize a smaller components)
     *
     * @return
     *      never null.
     *
     * @see Accessor#optimize(JAXBContextImpl)
     */
    Accessor getAccessor();

    /**
     * Returns true if this property has an element-only content. False otherwise.
     */
    public boolean elementOnlyContent();

    /**
     * Gets the "raw" type of the field.
     *
     * The raw type is the actual signature of the property.
     * For example, if the field is the primitive int, this will be the primitive int.
     * If the field is Object, this will be Object.
     * If the property is the collection and typed as {@code Collection<Integer>},
     * this method returns {@code Collection<Integer>}.
     *
     * @return always non-null.
     */
    Type getRawType();

    /**
     * Gets the type of the individual item.
     *
     * The individual type is the signature of the property used to store individual
     * values. For a non-collection field, this is the same as {@link #getRawType()}.
     * For acollection property, this is the type used to store individual value.
     * So if {@link #getRawType()} is {@code Collection<Integer>}, this method will
     * return {@link Integer}.
     *
     * @return always non-null.
     */
    Type getIndividualType();
}
