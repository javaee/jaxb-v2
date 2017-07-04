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

package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link Annotation} that also implements {@link Locatable}.
 *
 * @author Kohsuke Kawaguchi
 */
public class LocatableAnnotation implements InvocationHandler, Locatable, Location {
    private final Annotation core;

    private final Locatable upstream;

    /**
     * Wraps the annotation into a proxy so that the returned object will also implement
     * {@link Locatable}.
     */
    public static <A extends Annotation> A create( A annotation, Locatable parentSourcePos ) {
        if(annotation==null)    return null;
        Class<? extends Annotation> type = annotation.annotationType();
        if(quicks.containsKey(type)) {
            // use the existing proxy implementation if available
            return (A)quicks.get(type).newInstance(parentSourcePos,annotation);
        }

        // otherwise take the slow route

        ClassLoader cl = SecureLoader.getClassClassLoader(LocatableAnnotation.class);

        try {
            Class loadableT = Class.forName(type.getName(), false, cl);
            if(loadableT !=type)
                return annotation;  // annotation type not loadable from this class loader

            return (A)Proxy.newProxyInstance(cl,
                    new Class[]{ type, Locatable.class },
                    new LocatableAnnotation(annotation,parentSourcePos));
        } catch (ClassNotFoundException e) {
            // annotation not loadable
            return annotation;
        } catch (IllegalArgumentException e) {
            // Proxy.newProxyInstance throws this if it cannot resolve this annotation
            // in this classloader
            return annotation;
        }

    }

    LocatableAnnotation(Annotation core, Locatable upstream) {
        this.core = core;
        this.upstream = upstream;
    }

    public Locatable getUpstream() {
        return upstream;
    }

    public Location getLocation() {
        return this;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if(method.getDeclaringClass()==Locatable.class)
                return method.invoke(this,args);
            if(Modifier.isStatic(method.getModifiers()))
                // malicious code can pass in a static Method object.
                // doing method.invoke() would end up executing it,
                // so we need to protect against it.
                throw new IllegalArgumentException();

            return method.invoke(core,args);
        } catch (InvocationTargetException e) {
            if(e.getTargetException()!=null)
                throw e.getTargetException();
            throw e;
        }
    }

    public String toString() {
        return core.toString();
    }


    /**
     * List of {@link Quick} implementations keyed by their annotation type.
     */
    private static final Map<Class,Quick> quicks = new HashMap<Class, Quick>();

    static {
        for( Quick q : Init.getAll() ) {
            quicks.put(q.annotationType(),q);
        }
    }
}
