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

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.impl.ModelBuilderI;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * Reference to a type in a model.
 *
 * TODO: isn't there a similarity between this and TypeUse in XJC?
 *
 * @author Kohsuke Kawaguchi
 */
public final class Ref<T,C> {
    /**
     * The type being referenced.
     * <p>
     * If the type is adapted, this field is the same as the adapter's default type.
     */
    public final T type;
    /**
     * If the reference has an adapter, non-null.
     */
    public final Adapter<T,C> adapter;
    /**
     * If the {@link #type} is an array and it is a value list,
     * true.
     */
    public final boolean valueList;

    public Ref(T type) {
        this(type,null,false);
    }

    public Ref(T type, Adapter<T, C> adapter, boolean valueList) {
        this.adapter = adapter;
        if(adapter!=null)
            type=adapter.defaultType;
        this.type = type;
        this.valueList = valueList;
    }

    public Ref(ModelBuilderI<T,C,?,?> builder, T type, XmlJavaTypeAdapter xjta, XmlList xl ) {
        this(builder.getReader(),builder.getNavigator(),type,xjta,xl);
    }

    public Ref(AnnotationReader<T,C,?,?> reader,
               Navigator<T,C,?,?> nav,
               T type, XmlJavaTypeAdapter xjta, XmlList xl ) {
        Adapter<T,C> adapter=null;
        if(xjta!=null) {
            adapter = new Adapter<T,C>(xjta,reader,nav);
            type = adapter.defaultType;
        }

        this.type = type;
        this.adapter = adapter;
        this.valueList = xl!=null;
    }
}
