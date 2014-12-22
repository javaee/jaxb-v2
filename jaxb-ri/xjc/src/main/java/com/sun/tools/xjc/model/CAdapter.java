/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.model.nav.EagerNClass;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.Adapter;

/**
 * Extended {@link Adapter} for use within XJC.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CAdapter extends Adapter<NType,NClass> {

    /**
     * If non-null, the same as {@link #adapterType} but more conveniently typed.
     */
    private JClass adapterClass1;

    /**
     * If non-null, the same as {@link #adapterType} but more conveniently typed.
     */
    private Class<? extends XmlAdapter> adapterClass2;

    /**
     * When the adapter class is statically known to us.
     *
     * @param copy
     *      true to copy the adapter class into the user package,
     *      or otherwise just refer to the class specified via the
     *      adapter parameter.
     */
    public CAdapter(Class<? extends XmlAdapter> adapter, boolean copy) {
        super(getRef(adapter,copy),NavigatorImpl.theInstance);
        this.adapterClass1 = null;
        this.adapterClass2 = adapter;
    }

    static NClass getRef( final Class<? extends XmlAdapter> adapter, boolean copy ) {
        if(copy) {
            // TODO: this is a hack. the code generation should be defered until
            // the backend. (right now constant generation happens in the front-end)
            return new EagerNClass(adapter) {
                @Override
                public JClass toType(Outline o, Aspect aspect) {
                    return o.addRuntime(adapter);
                }
                public String fullName() {
                    // TODO: implement this method later
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return NavigatorImpl.theInstance.ref(adapter);
        }
    }

    public CAdapter(JClass adapter) {
        super( NavigatorImpl.theInstance.ref(adapter), NavigatorImpl.theInstance);
        this.adapterClass1 = adapter;
        this.adapterClass2 = null;
    }

    public JClass getAdapterClass(Outline o) {
        if(adapterClass1==null)
            adapterClass1 = o.getCodeModel().ref(adapterClass2);
        return adapterType.toType(o, Aspect.EXPOSED);
    }

    /**
     * Returns true if the adapter is for whitespace normalization.
     * Such an adapter can be ignored when producing a list.
     */
    public boolean isWhitespaceAdapter() {
        return adapterClass2==CollapsedStringAdapter.class || adapterClass2==NormalizedStringAdapter.class;
    }

    /**
     * Returns the adapter class if the adapter type is statically known to XJC.
     * <p>
     * This method is mostly for enabling certain optimized code generation.
     */
    public Class<? extends XmlAdapter> getAdapterIfKnown() {
        return adapterClass2;
    }
}
