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
}
