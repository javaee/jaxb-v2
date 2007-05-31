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

package com.sun.tools.xjc.api;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.SourcePosition;

/**
 * Reference to a JAXB type (from JAX-RPC.)
 *
 * <p>
 * A reference is a Java type (represented as a {@link TypeMirror})
 * and a set of annotations (represented as a {@link Declaration}).
 * Together they describe a root reference to a JAXB type binding.
 *
 * <p>
 * Those two values can be supplied independently, or you can use
 * other convenience constructors to supply two values at once.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public final class Reference {
    /**
     * The JAXB type being referenced. Must not be null.
     */
    public final TypeMirror type;
    /**
     * The declaration from which annotations for the {@link #type} is read.
     * Must not be null.
     */
    public final Declaration annotations;

    /**
     * Creates a reference from the return type of the method
     * and annotations on the method.
     */
    public Reference(MethodDeclaration method) {
        this(method.getReturnType(),method);
    }

    /**
     * Creates a reference from the parameter type
     * and annotations on the parameter.
     */
    public Reference(ParameterDeclaration param) {
        this(param.getType(),param);
    }

    /**
     * Creates a reference from a class declaration and its annotations.
     */
    public Reference(TypeDeclaration type,AnnotationProcessorEnvironment env) {
        this(env.getTypeUtils().getDeclaredType(type),type);
    }

    /**
     * Creates a reference by providing two values independently.
     */
    public Reference(TypeMirror type, Declaration annotations) {
        if(type==null || annotations==null)
            throw new IllegalArgumentException();
        this.type = type;
        this.annotations = annotations;
    }

    /**
     * Gets the source location that can be used to report error messages regarding
     * this reference.
     */
    public SourcePosition getPosition() {
        return annotations.getPosition();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reference)) return false;

        final Reference that = (Reference) o;

        return annotations.equals(that.annotations) && type.equals(that.type);
    }

    public int hashCode() {
        return 29 * type.hashCode() + annotations.hashCode();
    }
}
