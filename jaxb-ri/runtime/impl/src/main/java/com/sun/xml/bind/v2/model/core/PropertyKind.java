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

import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlInlineBinaryData;

/**
 * An Enum that indicates if the property is
 * Element, ElementRef, Value, or Attribute.
 *
 * <p>
 * Corresponds to the four different kind of {@link PropertyInfo}.
 * @author Bhakti Mehta (bhakti.mehta@sun.com)
 */
public enum PropertyKind {
    VALUE(true,false,Integer.MAX_VALUE),
    ATTRIBUTE(false,false,Integer.MAX_VALUE),
    ELEMENT(true,true,0),
    REFERENCE(false,true,1),
    MAP(false,true,2),
    ;

    /**
     * This kind of property can have {@link XmlMimeType} and {@link XmlInlineBinaryData}
     * annotation with it.
     */
    public final boolean canHaveXmlMimeType;

    /**
     * This kind of properties need to show up in {@link XmlType#propOrder()}.
     */
    public final boolean isOrdered;

    /**
     * {@code com.sun.xml.bind.v2.runtime.property.PropertyFactory} benefits from having index numbers assigned to
     * {@link #ELEMENT}, {@link #REFERENCE}, and {@link #MAP} in this order.
     */
    public final int propertyIndex;

    PropertyKind(boolean canHaveExpectedContentType, boolean isOrdered, int propertyIndex) {
        this.canHaveXmlMimeType = canHaveExpectedContentType;
        this.isOrdered = isOrdered;
        this.propertyIndex = propertyIndex;
    }
}
