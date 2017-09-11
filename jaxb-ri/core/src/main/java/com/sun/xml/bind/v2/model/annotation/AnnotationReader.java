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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.istack.Nullable;
import com.sun.xml.bind.v2.model.core.ErrorHandler;

/**
 * Reads annotations for the given property.
 *
 * <p>
 * This is the lowest abstraction that encapsulates the difference
 * between reading inline annotations and external binding files.
 *
 * <p>
 * Because the former operates on a {@link Field} and {@link Method}
 * while the latter operates on a "property", the methods defined
 * on this interface takes both, and the callee gets to choose which
 * to use.
 *
 * <p>
 * Most of the get method takes {@link Locatable}, which points to
 * the place/context in which the annotation is read. The returned
 * annotation also implements {@link Locatable} (so that it can
 * point to the place where the annotation is placed), and its
 * {@link Locatable#getUpstream()} will return the given
 * {@link Locatable}.
 *
 *
 * <p>
 * Errors found during reading annotations are reported through the error handler.
 * A valid {@link ErrorHandler} must be registered before the {@link AnnotationReader}
 * is used.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface AnnotationReader<T,C,F,M> {

    /**
     * Sets the error handler that receives errors found
     * during reading annotations.
     *
     * @param errorHandler
     *      must not be null.
     */
    void setErrorHandler(ErrorHandler errorHandler);

    /**
     * Reads an annotation on a property that consists of a field.
     */
    <A extends Annotation> A getFieldAnnotation(Class<A> annotation,
                                                F field, Locatable srcpos);

    /**
     * Checks if the given field has an annotation.
     */
    boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, F field);

    /**
     * Checks if a class has the annotation.
     */
    boolean hasClassAnnotation(C clazz, Class<? extends Annotation> annotationType);

    /**
     * Gets all the annotations on a field.
     */
    Annotation[] getAllFieldAnnotations(F field, Locatable srcPos);

    /**
     * Reads an annotation on a property that consists of a getter and a setter.
     *
     */
    <A extends Annotation> A getMethodAnnotation(Class<A> annotation,
                                                 M getter, M setter, Locatable srcpos);

    /**
     * Checks if the given method has an annotation.
     */
    boolean hasMethodAnnotation(Class<? extends Annotation> annotation, String propertyName, M getter, M setter, Locatable srcPos);

    /**
     * Gets all the annotations on a method.
     *
     * @param srcPos
     *      the location from which this annotation is read.
     */
    Annotation[] getAllMethodAnnotations(M method, Locatable srcPos);

    // TODO: we do need this to read certain annotations,
    // but that shows inconsistency wrt the spec. consult the spec team about the abstraction.
    <A extends Annotation> A getMethodAnnotation(Class<A> annotation, M method, Locatable srcpos );

    boolean hasMethodAnnotation(Class<? extends Annotation> annotation, M method );

    /**
     * Reads an annotation on a parameter of the method.
     *
     * @return null
     *      if the annotation was not found.
     */
    @Nullable
    <A extends Annotation> A getMethodParameterAnnotation(
            Class<A> annotation, M method, int paramIndex, Locatable srcPos );

    /**
     * Reads an annotation on a class.
     */
    @Nullable
    <A extends Annotation> A getClassAnnotation(Class<A> annotation, C clazz, Locatable srcpos) ;

    /**
     * Reads an annotation on the package that the given class belongs to.
     */
    @Nullable
    <A extends Annotation> A getPackageAnnotation(Class<A> annotation, C clazz, Locatable srcpos);

    /**
     * Reads a value of an annotation that returns a Class object.
     *
     * <p>
     * Depending on the underlying reflection library, you can't always
     * obtain the {@link Class} object directly (see the Annotation Processing MirrorTypeException
     * for example), so use this method to avoid that.
     *
     * @param name
     *      The name of the annotation parameter to be read.
     */
    T getClassValue( Annotation a, String name );

    /**
     * Similar to {@link #getClassValue(Annotation, String)} method but
     * obtains an array parameter.
     */
    T[] getClassArrayValue( Annotation a, String name );
}
