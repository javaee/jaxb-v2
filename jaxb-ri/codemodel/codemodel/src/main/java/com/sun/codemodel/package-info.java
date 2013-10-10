/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

/**
 * <h1>Library for generating Java source code</h1>.
 *
 * <p>
 * CodeModel is a library that allows you to generate Java source
 * code in a type-safe fashion.
 *
 * <p>
 * With CodeModel, you build the java source code by first building AST,
 * then writing it out as text files that is Java source files.
 * The AST looks like this:
 *
 * {@DotDiagram
    digraph G {
        cls1 [label="JDefinedClass"];
        cls2 [label="JDefinedClass"];
        JCodeModel -> cls1 [label="generated class"];
        JCodeModel -> cls2 [label="generated class"];

        m1 [label="JMethod"];
        m2 [label="JMethod"];

        cls1 -> m1;
        cls1 -> m2;
        cls1 -> JField;

        m1 -> JVar [label="method parameter"];
        m1 -> JBlock [label="code"];
    }
 * }
 *
 * <p>
 * You bulid this tree mostly from top-down. So, you first create
 * a new {@link JDefinedClass} from {@link JCodeModel}, then you
 * create a {@link JMethod} from {@link JDefinedClass}, and so on.
 *
 * <p>
 * This design brings the following beneefits:
 *
 * <ul>
 *  <li>source code can be written in random order
 *  <li>generated source code nicely imports other classes
 *  <li>generated source code is lexically always correct
 *      (no unbalanced parenthesis, etc.)
 *  <li>code generation becomes relatively type-safe
 * </ul>
 *
 * The price you pay for that is
 * increased memory footprint and the generation speed.
 * See <a href="#performance">performance section</a> for
 * more discussions about the performance and possible improvements.
 *
 *
 * <h2>Using CodeModel</h2>
 * <p>
 * {@link com.sun.codemodel.JCodeModel} is the entry point to
 * the library. See its javadoc for more details about how to use
 * CodeModel.
 *
 *
 *
 * <h2>Performance</h2>
 * <p>
 * Generally speaking, CodeModel is expected to be used in
 * an environment where the resource constraint is not severe.
 * Therefore, we haven't spent much effort in trying to make
 * this library lean and mean.
 *
 * <p>
 * That said, we did some benchmark and performance analysis.
 * In case anyone is interested in making this library
 * better performance wise, here's the findings.
 *
 * <p>
 * {@link List}s {@link Map}s, and other collections take up
 * a lot of space. Allocating those things lazily is generally
 * a good idea.
 *
 * <p>
 * Compared to template-based code generator, the writing operation
 * is slow, as it needs to traverse each AST node. Consider
 * pre-encoding tokens (like 'public') to the target encoding,
 * and consider exploting the subtree equivalence.
 *
 * @ArchitectureDocument
 */
package com.sun.codemodel;

import java.util.List;
import java.util.Map;
