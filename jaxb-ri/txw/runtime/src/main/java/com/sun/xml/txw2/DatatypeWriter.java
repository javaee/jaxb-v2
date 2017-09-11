/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.txw2;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Pluggable datatype writer.
 *
 * @author Kohsuke Kawaguchi
 */
public interface DatatypeWriter<DT> {

    /**
     * Gets the Java class that this writer can write.
     *
     * @return
     *      must not be null. Must be the same value always.
     */
    Class<DT> getType();

    /**
     * Prints the given datatype object and appends that result
     * into the given buffer.
     *
     * @param dt
     *      the datatype object to be printed.
     * @param resolver
     *      allows the converter to declare additional namespace prefixes.
     */
    void print(DT dt, NamespaceResolver resolver, StringBuilder buf);

    static final List<DatatypeWriter<?>> BUILTIN = Collections.unmodifiableList(new AbstractList() {
        
        private DatatypeWriter<?>[] BUILTIN_ARRAY = new DatatypeWriter<?>[] {
            new DatatypeWriter<String>() {
                public Class<String> getType() {
                    return String.class;
                }
                public void print(String s, NamespaceResolver resolver, StringBuilder buf) {
                    buf.append(s);
                }
            },
            new DatatypeWriter<Integer>() {
                public Class<Integer> getType() {
                    return Integer.class;
                }
                public void print(Integer i, NamespaceResolver resolver, StringBuilder buf) {
                    buf.append(i);
                }
            },
            new DatatypeWriter<Float>() {
                public Class<Float> getType() {
                    return Float.class;
                }
                public void print(Float f, NamespaceResolver resolver, StringBuilder buf) {
                    buf.append(f);
                }
            },
            new DatatypeWriter<Double>() {
                public Class<Double> getType() {
                    return Double.class;
                }
                public void print(Double d, NamespaceResolver resolver, StringBuilder buf) {
                    buf.append(d);
                }
            },
            new DatatypeWriter<QName>() {
                public Class<QName> getType() {
                    return QName.class;
                }
                public void print(QName qn, NamespaceResolver resolver, StringBuilder buf) {
                    String p = resolver.getPrefix(qn.getNamespaceURI());
                    if(p.length()!=0)
                        buf.append(p).append(':');
                    buf.append(qn.getLocalPart());
                }
            }
        };
                
        public DatatypeWriter<?> get(int n) { 
          return BUILTIN_ARRAY[n];
        }

        public int size() {
          return BUILTIN_ARRAY.length;
        }
      });
}
