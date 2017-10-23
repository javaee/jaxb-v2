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

package com.sun.xml.bind.v2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.bind.Util;

/**
 * Creates new instances of classes.
 *
 * <p>
 * This code handles the case where the class is not public or the constructor is
 * not public.
 *
 * @since 2.0
 * @author Kohsuke Kawaguchi
 */
public final class ClassFactory {
    private static final Class[] emptyClass = new Class[0];
    private static final Object[] emptyObject = new Object[0];

    private static final Logger logger = Util.getClassLogger();

    /**
     * Cache from a class to its default constructor.
     *
     * To avoid synchronization among threads, we use {@link ThreadLocal}.
     */
    private static final ThreadLocal<Map<Class, WeakReference<Constructor>>> tls = new ThreadLocal<Map<Class,WeakReference<Constructor>>>() {
        @Override
        public Map<Class,WeakReference<Constructor>> initialValue() {
            return new WeakHashMap<Class,WeakReference<Constructor>>();
        }
    };

    public static void cleanCache() {
        if (tls != null) {
            try {
                tls.remove();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unable to clean Thread Local cache of classes used in Unmarshaller: {0}", e.getLocalizedMessage());
            }
        }
    }

    /**
     * Creates a new instance of the class but throw exceptions without catching it.
     */
    public static <T> T create0( final Class<T> clazz ) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<Class,WeakReference<Constructor>> m = tls.get();
        Constructor<T> cons = null;
        WeakReference<Constructor> consRef = m.get(clazz);
        if(consRef!=null)
            cons = consRef.get();
        if(cons==null) {
            if (System.getSecurityManager() == null) {
                cons = tryGetDeclaredConstructor(clazz);
            } else {
                cons = AccessController.doPrivileged(new PrivilegedAction<Constructor<T>>() {
                    @Override
                    public Constructor<T> run() {
                        return tryGetDeclaredConstructor(clazz);
                    }
                });
            }

            int classMod = clazz.getModifiers();

            if(!Modifier.isPublic(classMod) || !Modifier.isPublic(cons.getModifiers())) {
                // attempt to make it work even if the constructor is not accessible
                try {
                    cons.setAccessible(true);
                } catch(SecurityException e) {
                    // but if we don't have a permission to do so, work gracefully.
                    logger.log(Level.FINE,"Unable to make the constructor of "+clazz+" accessible",e);
                    throw e;
                }
            }

            m.put(clazz,new WeakReference<Constructor>(cons));
        }

        return cons.newInstance(emptyObject);
    }

    private static <T> Constructor<T> tryGetDeclaredConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor((Class<T>[])emptyClass);
        } catch (NoSuchMethodException e) {
            logger.log(Level.INFO,"No default constructor found on "+clazz,e);
            NoSuchMethodError exp;
            if(clazz.getDeclaringClass()!=null && !Modifier.isStatic(clazz.getModifiers())) {
                exp = new NoSuchMethodError(Messages.NO_DEFAULT_CONSTRUCTOR_IN_INNER_CLASS
                                                    .format(clazz.getName()));
            } else {
                exp = new NoSuchMethodError(e.getMessage());
            }
            exp.initCause(e);
            throw exp;
        }
    }

    /**
     * The same as {@link #create0} but with an error handling to make
     * the instantiation error fatal.
     */
    public static <T> T create( Class<T> clazz ) {
        try {
            return create0(clazz);
        } catch (InstantiationException e) {
            logger.log(Level.INFO,"failed to create a new instance of "+clazz,e);
            throw new InstantiationError(e.toString());
        } catch (IllegalAccessException e) {
            logger.log(Level.INFO,"failed to create a new instance of "+clazz,e);
            throw new IllegalAccessError(e.toString());
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();

            // most likely an error on the user's code.
            // just let it through for the ease of debugging
            if(target instanceof RuntimeException)
                throw (RuntimeException)target;

            // error. just forward it for the ease of debugging
            if(target instanceof Error)
                throw (Error)target;

            // a checked exception.
            // not sure how we should report this error,
            // but for now we just forward it by wrapping it into a runtime exception
            throw new IllegalStateException(target);
        }
    }

    /**
     *  Call a method in the factory class to get the object.
     */
    public static Object create(Method method) {
        Throwable errorMsg;
        try {
            return method.invoke(null, emptyObject);
        } catch (InvocationTargetException ive) {
            Throwable target = ive.getTargetException();

            if(target instanceof RuntimeException)
                throw (RuntimeException)target;

            if(target instanceof Error)
                throw (Error)target;

            throw new IllegalStateException(target);
        } catch (IllegalAccessException e) {
            logger.log(Level.INFO,"failed to create a new instance of "+method.getReturnType().getName(),e);
            throw new IllegalAccessError(e.toString());
        } catch (IllegalArgumentException iae){
            logger.log(Level.INFO,"failed to create a new instance of "+method.getReturnType().getName(),iae);
            errorMsg = iae;
        } catch (NullPointerException npe){
            logger.log(Level.INFO,"failed to create a new instance of "+method.getReturnType().getName(),npe);
            errorMsg = npe;
        } catch (ExceptionInInitializerError eie){
            logger.log(Level.INFO,"failed to create a new instance of "+method.getReturnType().getName(),eie);
            errorMsg = eie;
        }

        NoSuchMethodError exp;
        exp = new NoSuchMethodError(errorMsg.getMessage());
        exp.initCause(errorMsg);
        throw exp;
    }
    
    /**
     * Infers the instanciable implementation class that can be assigned to the given field type.
     *
     * @return null
     *      if inference fails.
     */
    public static <T> Class<? extends T> inferImplClass(Class<T> fieldType, Class[] knownImplClasses) {
        if(!fieldType.isInterface())
            return fieldType;

        for( Class<?> impl : knownImplClasses ) {
            if(fieldType.isAssignableFrom(impl))
                return impl.asSubclass(fieldType);
        }

        // if we can't find an implementation class,
        // let's just hope that we will never need to create a new object,
        // and returns null
        return null;
    }
}
