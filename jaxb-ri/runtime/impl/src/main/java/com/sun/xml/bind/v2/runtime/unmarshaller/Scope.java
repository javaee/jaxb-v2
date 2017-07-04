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

package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.Lister;

import org.xml.sax.SAXException;

/**
 * Holds the information about packing scope.
 *
 * <p>
 * When no packing is started yet, all the fields should be set to null.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Scope<BeanT,PropT,ItemT,PackT> {

    public final UnmarshallingContext context;

    private BeanT bean;
    private Accessor<BeanT,PropT> acc;
    private PackT pack;
    private Lister<BeanT,PropT,ItemT,PackT> lister;

    Scope(UnmarshallingContext context) {
        this.context = context;
    }

    /**
     * Returns true if this scope object is filled by a packing in progress.
     */
    public boolean hasStarted() {
        return bean!=null;
    }

    /**
     * Initializes all the fields to null.
     */
    public void reset() {
        if(bean==null) {
            // already initialized
            assert clean();
            return;
        }

        bean = null;
        acc = null;
        pack = null;
        lister = null;
    }

    /**
     * Finishes up the current packing in progress (if any) and
     * resets this object.
     */
    public void finish() throws AccessorException {
        if(hasStarted()) {
            lister.endPacking(pack,bean,acc);
            reset();
        }
        assert clean();
    }

    private boolean clean() {
        return bean==null && acc==null && pack==null && lister==null;
    }

    /**
     * Adds a new item to this packing scope.
     */
    public void add( Accessor<BeanT,PropT> acc, Lister<BeanT,PropT,ItemT,PackT> lister, ItemT value) throws SAXException{
        try {
            if(!hasStarted()) {
                this.bean = (BeanT)context.getCurrentState().getTarget();
                this.acc = acc;
                this.lister = lister;
                this.pack = lister.startPacking(bean,acc);
            }

            lister.addToPack(pack,value);
        } catch (AccessorException e) {
            Loader.handleGenericException(e,true);
            // recover from this error by ignoring future items.
            this.lister = Lister.getErrorInstance();
            this.acc = Accessor.getErrorInstance();
        }
    }

    /**
     * Starts the packing scope, without adding any item.
     *
     * This allows us to return an empty pack, thereby allowing the user
     * to distinguish empty array vs null array.
     */
    public void start( Accessor<BeanT,PropT> acc, Lister<BeanT,PropT,ItemT,PackT> lister) throws SAXException{
        try {
            if(!hasStarted()) {
                this.bean = (BeanT)context.getCurrentState().getTarget();
                this.acc = acc;
                this.lister = lister;
                this.pack = lister.startPacking(bean,acc);
            }
        } catch (AccessorException e) {
            Loader.handleGenericException(e,true);
            // recover from this error by ignoring future items.
            this.lister = Lister.getErrorInstance();
            this.acc = Accessor.getErrorInstance();
        }
    }
}
