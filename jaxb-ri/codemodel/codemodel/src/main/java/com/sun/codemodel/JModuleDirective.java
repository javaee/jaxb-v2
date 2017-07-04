/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.codemodel;

// Currently only exports directive is needed in this model.
/**
 * Represents a Java module directive.
 * For example {@code "exports foo.bar;"} or {@code "requires foo.baz;"}.
 * @author Tomas Kraus
 */
public abstract class JModuleDirective {

    // Only ExportsDirective is implemented.
    /**
     * Module directive type. Child class implements {@code getType()} method which returns corresponding value.
     */
    public enum Type {
       /** Directive starting with {@code requires} keyword. */
       RequiresDirective,
       /** Directive starting with {@code exports} keyword. */
       ExportsDirective,
    }

    /** Name argument of module directive. */
    protected final String name;

    /**
     * Creates an instance of Java module directive.
     * @param name name argument of module directive.
     * @throws IllegalArgumentException if the name argument is {@code null}.
     */
    JModuleDirective(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("JModuleDirective name argument is null");
        }
        this.name = name;
    }

    /**
     * Gets the type of this module directive.
     * @return type of this module directive. Will never be {@code null}.
     */
    public abstract Type getType();

    /**
     * Print source code of this module directive.
     * @param f Java code formatter.
     * @return provided instance of Java code formatter.
     */
    public abstract JFormatter generate(final JFormatter f);

    /**
     * Compares this module directive to the specified object.
     * @param other The object to compare this {@link JModuleDirective} against.
     * @return {@code true} if the argument is not {@code null}
     *         and is a {@link JModuleDirective} object with the same type
     *         and equal name.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof JModuleDirective) {
            final JModuleDirective otherDirective = (JModuleDirective)other;
            return this.getType() == otherDirective.getType() && this.name.equals(otherDirective.name);
        }
        return false;
    }


    /**
     * Returns a hash code for this module directive based on directive type and name.
     * The hash code for a module directive is computed as
     * <blockquote><pre>
     *     {@code 97 * (type_ordinal_value + 1) + name.hashCode()}
     * </pre></blockquote>
     * using {@code int} arithmetic.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return 97 * (getType().ordinal() + 1) + name.hashCode();
    }

    /**
     * Gets the name of this module directive.
     * @return name of this module directive.
     */
    public String name() {
        return name;
    }

}
