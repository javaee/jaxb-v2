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

import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * {@link PropertyInfo} that holds references to other {@link Element}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ReferencePropertyInfo<T,C> extends PropertyInfo<T,C> {
    /**
     * Returns the information about the possible elements in this property.
     *
     * <p>
     * As of 2004/08/17, the spec only allows you to use different element names
     * when a property is a collection, but I think there's really no reason
     * to limit it there --- if the user wants to use a different tag name
     * for different objects, I don't see why this can be limited to collections.
     *
     * <p>
     * So this is a generalization of the spec. We always allow a property to have
     * multiple types and use different tag names for it, depending on the actual type.
     *
     * <p>
     * In most of the cases, this collection only contains 1 item. So the runtime system
     * is encouraged to provide a faster code-path that is optimized toward such cases.
     *
     * @return
     *      Always non-null. Contains at least one entry.
     */
    Set<? extends Element<T,C>> getElements();

    /**
     * {@inheritDoc}.
     *
     * If this {@link ReferencePropertyInfo} has a wildcard in it,
     * then the returned list will contain {@link WildcardTypeInfo}. 
     */
    Collection<? extends TypeInfo<T,C>> ref();

    /**
     * Gets the wrapper element name.
     *
     * @return
     *      must be null if not collection. If the property is a collection,
     *      this can be null (in which case there'll be no wrapper),
     *      or it can be non-null (in which case there'll be a wrapper)
     */
    QName getXmlName();

    /**
     * Returns true if this property is nillable
     * (meaning the absence of the value is treated as nil='true')
     *
     * <p>
     * This method is only used when this property is a collection.
     */
    boolean isCollectionNillable();

    /**
     * Checks if the wrapper element is required.
     *
     * @return
     *      Always false if {@link #getXmlName()}==null.
     */
    boolean isCollectionRequired();

    /**
     * Returns true if this property can hold {@link String}s to represent
     * mixed content model.
     */
    boolean isMixed();

    /**
     * If this property supports the wildcard, returns its mode.
     *
     * @return null
     *      if the wildcard is not allowed on this element.
     */
    WildcardMode getWildcard();

    /**
     * If this property supports the wildcard, returns its DOM handler.
     *
     * @return null
     *      if the wildcard is not allowed on this element.
     */
    C getDOMHandler();

    /**
     * Returns true if this element is mandatory.
     */
    boolean isRequired();

    Adapter<T,C> getAdapter();
}
