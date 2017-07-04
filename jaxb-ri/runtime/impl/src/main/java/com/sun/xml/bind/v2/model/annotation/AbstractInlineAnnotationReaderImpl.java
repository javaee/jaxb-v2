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

import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;

/**
 * {@link AnnotationReader} that reads annotation from classes,
 * not from external binding files.
 *
 * This is meant to be used as a convenient partial implementation.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class AbstractInlineAnnotationReaderImpl<T,C,F,M>
    implements AnnotationReader<T,C,F,M> {

    private ErrorHandler errorHandler;

    public void setErrorHandler(ErrorHandler errorHandler) {
        if(errorHandler==null)
            throw new IllegalArgumentException();
        this.errorHandler = errorHandler;
    }

    /**
     * Always return a non-null valid {@link ErrorHandler}
     */
    public final ErrorHandler getErrorHandler() {
        assert errorHandler!=null : "error handler must be set before use";
        return errorHandler;
    }

    public final <A extends Annotation> A getMethodAnnotation(Class<A> annotation, M getter, M setter, Locatable srcPos) {
        A a1 = getter==null?null:getMethodAnnotation(annotation,getter,srcPos);
        A a2 = setter==null?null:getMethodAnnotation(annotation,setter,srcPos);

        if(a1==null) {
            if(a2==null)
                return null;
            else
                return a2;
        } else {
            if(a2==null)
                return a1;
            else {
                // both are present
                getErrorHandler().error(new IllegalAnnotationException(
                    Messages.DUPLICATE_ANNOTATIONS.format(
                        annotation.getName(), fullName(getter),fullName(setter)),
                    a1, a2 ));
                // recover by ignoring one of them
                return a1;
            }
        }
    }

    public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, String propertyName, M getter, M setter, Locatable srcPos) {
        boolean x = ( getter != null && hasMethodAnnotation(annotation, getter) );
        boolean y = ( setter != null && hasMethodAnnotation(annotation, setter) );

        if(x && y) {
            // both are present. have getMethodAnnotation report an error
            getMethodAnnotation(annotation,getter,setter,srcPos);
        }

        return x||y;
    }

    /**
     * Gets the fully-qualified name of the method.
     *
     * Used for error messages.
     */
    protected abstract String fullName(M m);
}
