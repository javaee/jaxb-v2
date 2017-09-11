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

package com.sun.xml.bind.v2.runtime.reflect;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.runtime.Coordinator;

/**
 * {@link Accessor} that adapts the value by using {@link Adapter}.
 *
 * @see Accessor#adapt
 * @author Kohsuke Kawaguchi
 */
final class AdaptedAccessor<BeanT,InMemValueT,OnWireValueT> extends Accessor<BeanT,OnWireValueT> {
    private final Accessor<BeanT,InMemValueT> core;
    private final Class<? extends XmlAdapter<OnWireValueT,InMemValueT>> adapter;

    /*pacakge*/ AdaptedAccessor(Class<OnWireValueT> targetType, Accessor<BeanT, InMemValueT> extThis, Class<? extends XmlAdapter<OnWireValueT, InMemValueT>> adapter) {
        super(targetType);
        this.core = extThis;
        this.adapter = adapter;
    }

    @Override
    public boolean isAdapted() {
        return true;
    }

    public OnWireValueT get(BeanT bean) throws AccessorException {
        InMemValueT v = core.get(bean);

        XmlAdapter<OnWireValueT,InMemValueT> a = getAdapter();
        try {
            return a.marshal(v);
        } catch (Exception e) {
            throw new AccessorException(e);
        }
    }

    public void set(BeanT bean, OnWireValueT o) throws AccessorException {
        XmlAdapter<OnWireValueT, InMemValueT> a = getAdapter();
        try {
            core.set(bean, (o == null ? null : a.unmarshal(o)));
        } catch (Exception e) {
            throw new AccessorException(e);
        }
    }

    public Object getUnadapted(BeanT bean) throws AccessorException {
        return core.getUnadapted(bean);
    }

    public void setUnadapted(BeanT bean, Object value) throws AccessorException {
        core.setUnadapted(bean,value);
    }

    /**
     * Sometimes Adapters are used directly by JAX-WS outside any
     * {@link Coordinator}. Use this lazily-created cached
     * {@link XmlAdapter} in such cases.
     */
    private XmlAdapter<OnWireValueT, InMemValueT> staticAdapter;

    private XmlAdapter<OnWireValueT, InMemValueT> getAdapter() {
        Coordinator coordinator = Coordinator._getInstance();
        if(coordinator!=null)
            return coordinator.getAdapter(adapter);
        else {
            synchronized(this) {
                if(staticAdapter==null)
                    staticAdapter = ClassFactory.create(adapter);
            }
            return staticAdapter;
        }
    }
}
