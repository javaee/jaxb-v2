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

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.XMLSerializer;

/**
 * {@link Lister} for primitive type arrays.
 * <p><b>
 *     Auto-generated, do not edit.
 * </b></p>
 * <p>
 *     B y t e ArrayLister is used as the master to generate the rest of the
 *     lister classes. Do not modify the generated copies.
 * </p>
 */
final class PrimitiveArrayListerDouble<BeanT> extends Lister<BeanT,double[],Double,PrimitiveArrayListerDouble.DoubleArrayPack> {
    
    private PrimitiveArrayListerDouble() {
    }

    /*package*/ static void register() {
        Lister.primitiveArrayListers.put(Double.TYPE,new PrimitiveArrayListerDouble());
    }

    public ListIterator<Double> iterator(final double[] objects, XMLSerializer context) {
        return new ListIterator<Double>() {
            int idx=0;
            public boolean hasNext() {
                return idx<objects.length;
            }

            public Double next() {
                return objects[idx++];
            }
        };
    }

    public DoubleArrayPack startPacking(BeanT current, Accessor<BeanT, double[]> acc) {
        return new DoubleArrayPack();
    }

    public void addToPack(DoubleArrayPack objects, Double o) {
        objects.add(o);
    }

    public void endPacking( DoubleArrayPack pack, BeanT bean, Accessor<BeanT,double[]> acc ) throws AccessorException {
        acc.set(bean,pack.build());
    }

    public void reset(BeanT o,Accessor<BeanT,double[]> acc) throws AccessorException {
        acc.set(o,new double[0]);
    }

    static final class DoubleArrayPack {
        double[] buf = new double[16];
        int size;

        void add(Double b) {
            if(buf.length==size) {
                // realloc
                double[] nb = new double[buf.length*2];
                System.arraycopy(buf,0,nb,0,buf.length);
                buf = nb;
            }
            if(b!=null)
                buf[size++] = b;
        }

        double[] build() {
            if(buf.length==size)
                // if we are lucky enough
                return buf;

            double[] r = new double[size];
            System.arraycopy(buf,0,r,0,size);
            return r;
        }
    }
}
