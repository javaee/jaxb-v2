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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * @author Kohsuke Kawaguchi
 */
final class RuntimeElementInfoImpl extends ElementInfoImpl<Type,Class,Field,Method>
    implements RuntimeElementInfo {
    
    public RuntimeElementInfoImpl(RuntimeModelBuilder modelBuilder, RegistryInfoImpl registry, Method method) throws IllegalAnnotationException {
        super(modelBuilder, registry, method);

        Adapter<Type,Class> a = getProperty().getAdapter();

        if(a!=null)
            adapterType = a.adapterType;
        else
            adapterType = null;
    }

    @Override
    protected PropertyImpl createPropertyImpl() {
        return new RuntimePropertyImpl();
    }

    class RuntimePropertyImpl extends PropertyImpl implements RuntimeElementPropertyInfo, RuntimeTypeRef {
        public Accessor getAccessor() {
            if(adapterType==null)
                return Accessor.JAXB_ELEMENT_VALUE;
            else
                return Accessor.JAXB_ELEMENT_VALUE.adapt(
                        (Class)getAdapter().defaultType,(Class)adapterType);
        }

        public Type getRawType() {
            return Collection.class;
        }

        public Type getIndividualType() {
             return getContentType().getType();
        }


        public boolean elementOnlyContent() {
            return false;   // this method doesn't make sense here
        }

        public List<? extends RuntimeTypeRef> getTypes() {
            return Collections.singletonList(this);
        }

        public List<? extends RuntimeNonElement> ref() {
            return (List<? extends RuntimeNonElement>)super.ref();
        }

        public RuntimeNonElement getTarget() {
            return (RuntimeNonElement)super.getTarget();
        }

        public RuntimePropertyInfo getSource() {
            return this;
        }

        public Transducer getTransducer() {
            return RuntimeModelBuilder.createTransducer(this);
        }
    }

    /**
     * The adapter specified by <code>getProperty().getAdapter()</code>.
     */
    private final Class<? extends XmlAdapter> adapterType;

    public RuntimeElementPropertyInfo getProperty() {
        return (RuntimeElementPropertyInfo)super.getProperty();
    }

    public Class<? extends JAXBElement> getType() {
        //noinspection unchecked
        return (Class<? extends JAXBElement>) Utils.REFLECTION_NAVIGATOR.erasure(super.getType());
    }

    public RuntimeClassInfo getScope() {
        return (RuntimeClassInfo)super.getScope();
    }

    public RuntimeNonElement getContentType() {
        return (RuntimeNonElement)super.getContentType();
    }
}
