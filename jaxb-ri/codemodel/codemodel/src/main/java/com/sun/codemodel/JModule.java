/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.codemodel;

// Based on modules grammar from http://openjdk.java.net/projects/jigsaw/doc/lang-vm.html

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Java module.
 * @author Tomas Kraus
 */
public class JModule {

    /** Java module file name. */
    private static final String FILE_NAME = "module-info.java";

    /** Name of this module. Mandatory value. Shall not be {@code null}. */
    private final String name;

    /** {@link Set} of Java module directives. */
    private final Set<JModuleDirective> directives;

    /**
     * Creates an instance of Java module.
     * @param name Java module name. Value can not be {@code null}
     * @param version Java module version.
     */
    JModule(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Value of name is null");
        }
        this.name = name;
        this.directives = new HashSet<>();
    }

    /**
     * Gets the name of this module.
     * @return name of this module.
     */
    public String name() {
        return name;
    }

    /**
     * Gets module directives set.
     * jUnit helper method.
     * @return Module directives set.
     */
    Set<JModuleDirective> getDirectives() {
        return directives;
    }

    /**
     * Adds a package to the list of Java module exports.
     * The package name shall not be {@code null} or empty {@code String}.
     * @param pkg Java package to be exported.
     */
    public void _exports(final JPackage pkg) {
        directives.add(new JExportsDirective(pkg.name()));
    }

    /**
     * Adds a module to the list of Java module requirements.
     * The module name shall not be {@code null} or empty {@code String}.
     * @param name Name of required Java module.
     * @param isPublic Use {@code public} modifier.
     * @param isStatic Use {@code static} modifier.
     */
    public void _requires(final String name, final boolean isPublic, final boolean isStatic) {
        directives.add(new JRequiresDirective(name, isPublic, isStatic));
    }

    /**
     * Adds a module to the list of Java module requirements without {@code public} and {@code static} modifiers.
     * The module name shall not be {@code null} or empty {@code String}.
     * @param name Name of required Java module.
     */
    public void _requires(final String name) {
        directives.add(new JRequiresDirective(name, false, false));
    }

    /**
     * Print source code of Java Module declaration.
     * @param f Java code formatter.
     * @return provided instance of Java code formatter.
     */
    public JFormatter generate(final JFormatter f) {
        f.p("module").p(name);
        f.p('{').nl();
        if (!directives.isEmpty()) {
            f.i();
            for (final JModuleDirective directive : directives) {
                directive.generate(f);
            }
            f.o();
        }
        f.p('}').nl();
        return f;
    }

    /**
     * Create {@code module-info.java} source writer.
     * @return New instance of {@code module-info.java} source writer.
     */
    private JFormatter createModuleInfoSourceFileWriter(final CodeWriter src) throws IOException {
        Writer bw = new BufferedWriter(src.openSource(null, FILE_NAME));
        return new JFormatter(new PrintWriter(bw));
    }

    /**
     * Build {@code module-info.java} source file.
     * @param src Source code writer.
     * @throws IOException if there is any problem with writing the file.
     */
    void build(final CodeWriter src) throws IOException {
        final JFormatter f = createModuleInfoSourceFileWriter(src);
        generate(f);
        f.close();
    }

}
