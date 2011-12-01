/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jxc.ap;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.LocatableAnnotation;

/**
 * {@link AnnotationReader} implementation that reads annotation inline from Annoation Processing.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class InlineAnnotationReaderImpl extends AbstractInlineAnnotationReaderImpl<TypeMirror, TypeElement, VariableElement, ExecutableElement> {

    /** The singleton instance. */
    public static final InlineAnnotationReaderImpl theInstance = new InlineAnnotationReaderImpl();

    private InlineAnnotationReaderImpl() {}

    public <A extends Annotation> A getClassAnnotation(Class<A> a, TypeElement clazz, Locatable srcPos) {
        return LocatableAnnotation.create(clazz.getAnnotation(a),srcPos);
    }

    public <A extends Annotation> A getFieldAnnotation(Class<A> a, VariableElement f, Locatable srcPos) {
        return LocatableAnnotation.create(f.getAnnotation(a),srcPos);
    }

    public boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, VariableElement f) {
        return f.getAnnotation(annotationType)!=null;
    }

    public boolean hasClassAnnotation(TypeElement clazz, Class<? extends Annotation> annotationType) {
        return clazz.getAnnotation(annotationType)!=null;
    }

    public Annotation[] getAllFieldAnnotations(VariableElement field, Locatable srcPos) {
        return getAllAnnotations(field,srcPos);
    }

    public <A extends Annotation> A getMethodAnnotation(Class<A> a, ExecutableElement method, Locatable srcPos) {
        return LocatableAnnotation.create(method.getAnnotation(a),srcPos);
    }

    public boolean hasMethodAnnotation(Class<? extends Annotation> a, ExecutableElement method) {
        return method.getAnnotation(a)!=null;
    }

    public Annotation[] getAllMethodAnnotations(ExecutableElement method, Locatable srcPos) {
        return getAllAnnotations(method,srcPos);
    }

    /**
     * Gets all the annotations on the given declaration.
     */
    private Annotation[] getAllAnnotations(Element decl, Locatable srcPos) {
        List<Annotation> r = new ArrayList<Annotation>();

        for( AnnotationMirror m : decl.getAnnotationMirrors() ) {
            try {
                String fullName = ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString();
                Class<? extends Annotation> type =
                    SecureLoader.getClassClassLoader(getClass()).loadClass(fullName).asSubclass(Annotation.class);
                Annotation annotation = decl.getAnnotation(type);
                if(annotation!=null)
                    r.add( LocatableAnnotation.create(annotation,srcPos) );
            } catch (ClassNotFoundException e) {
                // just continue
            }
        }

        return r.toArray(new Annotation[r.size()]);
    }

    public <A extends Annotation> A getMethodParameterAnnotation(Class<A> a, ExecutableElement m, int paramIndex, Locatable srcPos) {
        VariableElement[] params = m.getParameters().toArray(new VariableElement[m.getParameters().size()]);
        return LocatableAnnotation.create(
            params[paramIndex].getAnnotation(a), srcPos );
    }

    public <A extends Annotation> A getPackageAnnotation(Class<A> a, TypeElement clazz, Locatable srcPos) {
        return LocatableAnnotation.create(clazz.getEnclosingElement().getAnnotation(a), srcPos);
    }

    public TypeMirror getClassValue(Annotation a, String name) {
        try {
            a.annotationType().getMethod(name).invoke(a);
            assert false;
            throw new IllegalStateException("should throw a MirroredTypeException");
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            if( e.getCause() instanceof MirroredTypeException ) {
                MirroredTypeException me = (MirroredTypeException)e.getCause();
                return me.getTypeMirror();
            }
            // impossible
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public TypeMirror[] getClassArrayValue(Annotation a, String name) {
        try {
            a.annotationType().getMethod(name).invoke(a);
            assert false;
            throw new IllegalStateException("should throw a MirroredTypesException");
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            if( e.getCause() instanceof MirroredTypesException ) {
                MirroredTypesException me = (MirroredTypesException)e.getCause();
                Collection<? extends TypeMirror> r = me.getTypeMirrors();
                return r.toArray(new TypeMirror[r.size()]);
            }
            // *********************** TODO: jdk6 bug. Fixed in java7
            // According to the javadocs it should throw the MirroredTypesException
            if( e.getCause() instanceof MirroredTypeException ) {
                MirroredTypeException me = (MirroredTypeException)e.getCause();
                TypeMirror tr = me.getTypeMirror();
                TypeMirror[] trArr = new TypeMirror[1];
                trArr[0] = tr;
                return trArr;
            }
            // *******************************************
            // impossible
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    protected String fullName(ExecutableElement m) {
        return ((TypeElement) m.getEnclosingElement()).getQualifiedName().toString()+'#'+m.getSimpleName();
    }
}
