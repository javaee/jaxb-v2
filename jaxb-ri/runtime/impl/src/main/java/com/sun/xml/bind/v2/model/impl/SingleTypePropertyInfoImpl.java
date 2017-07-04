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

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlList;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * {@link PropertyInfoImpl} that can only have one type.
 *
 * Specifically, {@link AttributePropertyInfoImpl} and {@link ValuePropertyInfoImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class SingleTypePropertyInfoImpl<T,C,F,M>
    extends PropertyInfoImpl<T,C,F,M> {

    /**
     * Computed lazily.
     *
     * @see {@link #getTarget()}.
     */
    private NonElement<T,C> type;

    public SingleTypePropertyInfoImpl(ClassInfoImpl<T,C,F,M> classInfo, PropertySeed<T,C,F,M> seed) {
        super(classInfo, seed);
        if(this instanceof RuntimePropertyInfo) {
            Accessor rawAcc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
            if(getAdapter()!=null && !isCollection())
                // adapter for a single-value property is handled by accessor.
                // adapter for a collection property is handled by lister.
                rawAcc = rawAcc.adapt(((RuntimePropertyInfo)this).getAdapter());
            this.acc = rawAcc;
        } else
            this.acc = null;
    }

    public List<? extends NonElement<T,C>> ref() {
        return Collections.singletonList(getTarget());
    }

    public NonElement<T,C> getTarget() {
        if(type==null) {
            assert parent.builder!=null : "this method must be called during the build stage";
            type = parent.builder.getTypeInfo(getIndividualType(),this);
        }
        return type;
    }

    public PropertyInfo<T,C> getSource() {
        return this;
    }

    public void link() {
        super.link();

        if (!(NonElement.ANYTYPE_NAME.equals(type.getTypeName()) || type.isSimpleType() || id()==ID.IDREF)) {
                parent.builder.reportError(new IllegalAnnotationException(
                Messages.SIMPLE_TYPE_IS_REQUIRED.format(),
                seed
            ));
        }

        if(!isCollection() && seed.hasAnnotation(XmlList.class)) {
            parent.builder.reportError(new IllegalAnnotationException(
                Messages.XMLLIST_ON_SINGLE_PROPERTY.format(), this
            ));
        }
    }

//
//
// technically these code belong to runtime implementation, but moving the code up here
// allows this to be shared between RuntimeValuePropertyInfoImpl and RuntimeAttributePropertyInfoImpl
//
//

    private final Accessor acc;
    /**
     * Lazily created.
     */
    private Transducer xducer;

    public Accessor getAccessor() {
        return acc;
    }


    public Transducer getTransducer() {
        if(xducer==null) {
            xducer = RuntimeModelBuilder.createTransducer((RuntimeNonElementRef)this);
            if(xducer==null) {
                // this situation is checked by by the link method.
                // avoid repeating the same error by silently recovering
                xducer = RuntimeBuiltinLeafInfoImpl.STRING;
            }
        }
        return xducer;
    }
}
