/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.jxc.apt;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.MirroredTypesException;
import com.sun.mirror.type.TypeMirror;
import com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.LocatableAnnotation;

/**
 * {@link AnnotationReader} implementation that reads annotation inline from APT.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class InlineAnnotationReaderImpl extends AbstractInlineAnnotationReaderImpl<TypeMirror,TypeDeclaration,FieldDeclaration,MethodDeclaration> {

    /** The singleton instance. */
    public static final InlineAnnotationReaderImpl theInstance = new InlineAnnotationReaderImpl();

    private InlineAnnotationReaderImpl() {}

    public <A extends Annotation> A getClassAnnotation(Class<A> a, TypeDeclaration clazz, Locatable srcPos) {
        return LocatableAnnotation.create(clazz.getAnnotation(a),srcPos);
    }

    public <A extends Annotation> A getFieldAnnotation(Class<A> a, FieldDeclaration f, Locatable srcPos) {
        return LocatableAnnotation.create(f.getAnnotation(a),srcPos);
    }

    public boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, FieldDeclaration f) {
        return f.getAnnotation(annotationType)!=null;
    }

    public boolean hasClassAnnotation(TypeDeclaration clazz, Class<? extends Annotation> annotationType) {
        return clazz.getAnnotation(annotationType)!=null;
    }

    public Annotation[] getAllFieldAnnotations(FieldDeclaration field, Locatable srcPos) {
        return getAllAnnotations(field,srcPos);
    }

    public <A extends Annotation> A getMethodAnnotation(Class<A> a, MethodDeclaration method, Locatable srcPos) {
        return LocatableAnnotation.create(method.getAnnotation(a),srcPos);
    }

    public boolean hasMethodAnnotation(Class<? extends Annotation> a, MethodDeclaration method) {
        return method.getAnnotation(a)!=null;
    }

    private static final Annotation[] EMPTY_ANNOTATION = new Annotation[0];

    public Annotation[] getAllMethodAnnotations(MethodDeclaration method, Locatable srcPos) {
        return getAllAnnotations(method,srcPos);
    }

    /**
     * Gets all the annotations on the given declaration.
     */
    private Annotation[] getAllAnnotations(Declaration decl, Locatable srcPos) {
        List<Annotation> r = new ArrayList<Annotation>();

        for( AnnotationMirror m : decl.getAnnotationMirrors() ) {
            try {
                String fullName = m.getAnnotationType().getDeclaration().getQualifiedName();
                Class<? extends Annotation> type =
                    getClass().getClassLoader().loadClass(fullName).asSubclass(Annotation.class);
                Annotation annotation = decl.getAnnotation(type);
                if(annotation!=null)
                    r.add( LocatableAnnotation.create(annotation,srcPos) );
            } catch (ClassNotFoundException e) {
                // just continue
            }
        }

        return r.toArray(EMPTY_ANNOTATION);
    }

    public <A extends Annotation> A getMethodParameterAnnotation(Class<A> a, MethodDeclaration m, int paramIndex, Locatable srcPos) {
        ParameterDeclaration[] params = m.getParameters().toArray(new ParameterDeclaration[0]);
        return LocatableAnnotation.create(
            params[paramIndex].getAnnotation(a), srcPos );
    }

    public <A extends Annotation> A getPackageAnnotation(Class<A> a, TypeDeclaration clazz, Locatable srcPos) {
        return LocatableAnnotation.create(clazz.getPackage().getAnnotation(a),srcPos);
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
                Collection<TypeMirror> r = me.getTypeMirrors();
                return r.toArray(new TypeMirror[r.size()]);
            }
            // impossible
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    protected String fullName(MethodDeclaration m) {
        return m.getDeclaringType().getQualifiedName()+'#'+m.getSimpleName();
    }
}
