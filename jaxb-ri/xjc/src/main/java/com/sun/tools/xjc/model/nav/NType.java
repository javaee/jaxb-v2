/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

/**
 * A type.
 *
 * See the package documentation for details.
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
