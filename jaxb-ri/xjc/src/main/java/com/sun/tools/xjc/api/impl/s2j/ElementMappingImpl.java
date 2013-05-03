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

package com.sun.tools.xjc.api.impl.s2j;

import java.util.List;

import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;

/**
 * @author Kohsuke Kawaguchi
 */
final class ElementMappingImpl extends AbstractMappingImpl<CElementInfo> {

    private final TypeAndAnnotation taa;

    protected ElementMappingImpl(JAXBModelImpl parent, CElementInfo elementInfo) {
        super(parent,elementInfo);

        TypeUse t = clazz.getContentType();
        if(clazz.getProperty().isCollection())
            t = TypeUseFactory.makeCollection(t);
        CAdapter a = clazz.getProperty().getAdapter();
        if(a!=null)
            t = TypeUseFactory.adapt(t,a);
        taa = new TypeAndAnnotationImpl(parent.outline,t);
    }

    public TypeAndAnnotation getType() {
        return taa;
    }

    public final List<Property> calcDrilldown() {
        CElementPropertyInfo p = clazz.getProperty();

        if(p.getAdapter()!=null)
            return null;    // if adapted, avoid drill down

        if(p.isCollection())
        // things like <xs:element name="foo" type="xs:NMTOKENS" /> is not eligible.
            return null;

        CTypeInfo typeClass = p.ref().get(0);

        if(!(typeClass instanceof CClassInfo))
            // things like <xs:element name="foo" type="xs:string" /> is not eligible.
            return null;

        CClassInfo ci = (CClassInfo)typeClass;

        // if the type is abstract we can't use it.
        if(ci.isAbstract())
            return null;

        // the 'all' compositor doesn't qualify
        if(!ci.isOrdered())
            return null;

        return buildDrilldown(ci);
    }
}
