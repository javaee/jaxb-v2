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

import java.lang.annotation.Annotation;
import java.beans.Introspector;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link PropertyInfo} implementation backed by a getter and a setter.
 *
 * We allow the getter or setter to be null, in which case the bean
 * can only participate in unmarshalling (or marshalling)
 */
class GetterSetterPropertySeed<TypeT,ClassDeclT,FieldT,MethodT> implements
        PropertySeed<TypeT,ClassDeclT,FieldT,MethodT> {

    protected final MethodT getter;
    protected final MethodT setter;
    private ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent;

    GetterSetterPropertySeed(ClassInfoImpl<TypeT,ClassDeclT,FieldT,MethodT> parent, MethodT getter, MethodT setter) {
        this.parent = parent;
        this.getter = getter;
        this.setter = setter;

        if(getter==null && setter==null)
            throw new IllegalArgumentException();
    }

    public TypeT getRawType() {
        if(getter!=null)
            return parent.nav().getReturnType(getter);
        else
            return parent.nav().getMethodParameters(setter)[0];
    }

    public <A extends Annotation> A readAnnotation(Class<A> annotation) {
        return parent.reader().getMethodAnnotation(annotation, getter,setter,this);
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return parent.reader().hasMethodAnnotation(annotationType,getName(),getter,setter,this);
    }

    public String getName() {
        if(getter!=null)
            return getName(getter);
        else
            return getName(setter);
    }

    private String getName(MethodT m) {
        String seed = parent.nav().getMethodName(m);
        String lseed = seed.toLowerCase();
        if(lseed.startsWith("get") || lseed.startsWith("set"))
            return camelize(seed.substring(3));
        if(lseed.startsWith("is"))
            return camelize(seed.substring(2));
        return seed;
    }


    private static String camelize(String s) {
        return Introspector.decapitalize(s);
    }

    /**
     * Use the enclosing class as the upsream {@link Location}.
     */
    public Locatable getUpstream() {
        return parent;
    }

    public Location getLocation() {
        if(getter!=null)
            return parent.nav().getMethodLocation(getter);
        else
            return parent.nav().getMethodLocation(setter);
    }
}
