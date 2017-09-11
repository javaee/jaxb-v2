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

package com.sun.xml.bind.v2.model.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.core.BuiltinLeafInfo;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * JAXB spec designates a few Java classes to be mapped to XML types
 * in a way that ignores restrictions placed on user-defined beans.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuiltinLeafInfoImpl<TypeT,ClassDeclT> extends LeafInfoImpl<TypeT,ClassDeclT> implements BuiltinLeafInfo<TypeT,ClassDeclT> {

    private final QName[] typeNames;

    protected BuiltinLeafInfoImpl(TypeT type, QName... typeNames) {
        super(type, typeNames.length>0?typeNames[0]:null);
        this.typeNames = typeNames;
    }

    /**
     * Returns all the type names recognized by this bean info.
     *
     * @return
     *      do not modify the returned array.
     */
    public final QName[] getTypeNames() {
        return typeNames;
    }

    /**
     * @deprecated always return false at this level.
     */
    public final boolean isElement() {
        return false;
    }

    /**
     * @deprecated always return null at this level.
     */
    public final QName getElementName() {
        return null;
    }

    /**
     * @deprecated always return null at this level.
     */
    public final Element<TypeT,ClassDeclT> asElement() {
        return null;
    }

    /**
     * Creates all the {@link BuiltinLeafInfoImpl}s as specified in the spec.
     *
     * {@link LeafInfo}s are all defined by the spec.
     */
    public static <TypeT,ClassDeclT>
    Map<TypeT,BuiltinLeafInfoImpl<TypeT,ClassDeclT>> createLeaves( Navigator<TypeT,ClassDeclT,?,?> nav ) {
        Map<TypeT,BuiltinLeafInfoImpl<TypeT,ClassDeclT>> leaves = new HashMap<TypeT,BuiltinLeafInfoImpl<TypeT,ClassDeclT>>();

        for( RuntimeBuiltinLeafInfoImpl<?> leaf : RuntimeBuiltinLeafInfoImpl.builtinBeanInfos ) {
            TypeT t = nav.ref(leaf.getClazz());
            leaves.put( t, new BuiltinLeafInfoImpl<TypeT,ClassDeclT>(t,leaf.getTypeNames()) );
        }

        return leaves;
    }
}
