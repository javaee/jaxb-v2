package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

/**
 * A type.
 *
 * See the package documentaion for details.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface NType {
    /**
     * Returns the representation of this type in code model.
     * <p>
     * This operation requires the whole model to be built,
     * and hence it takes {@link Outline}.
     * <p>
     * Under some code generation strategy, some bean classes
     * are considered implementation specific (such as impl.FooImpl class)
     * These classes always have accompanying "exposed" type (such as
     * the Foo interface).
     * <p>
     * For such Jekyll and Hyde type, the aspect parameter determines
     * which personality is returned.
     *
     * @param aspect
     *      If {@link Aspect#IMPLEMENTATION}, this method returns the
     *      implementation specific class that this type represents.
     *      If {@link Aspect#EXPOSED}, this method returns the
     *      publicly exposed type that this type represents.
     *
     *      For ordinary classes, the aspect parameter is meaningless.
     *
     */
    JType toType(Outline o, Aspect aspect);

    /**
     * Returns true iff this type represents a class that has a unboxed form.
     *
     * For example, for {@link String} this is false, but for {@link Integer}
     * this is true.
     */
    boolean isBoxedType();

    /**
     * Human readable name of this type.
     */
    String fullName();
}
