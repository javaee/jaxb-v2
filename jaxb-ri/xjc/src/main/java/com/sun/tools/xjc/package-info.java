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

/**
 * <h1>Schema to Java compiler</h1>.
 *
 * <p>
 * This module contains the code that implements the schema compiler 'XJC'.
 *
 *
 * <h2>XJC Architecture Diagram</h2>
 * {@DotDiagram
     digraph G {
         rankdir=TB;

         // data
         node [shape=box]; // style=filled,color=lightpink];
         schema -> "DOM forest" [label="DOMForest.parse()"];
         "DOM forest" -> "schema OM" [label="SOM specific parser"];
         "schema OM" -> model [label="language specific builder"];

         model -> codeModel [label="BeanGenerator.generate()"];
         codeModel -> "Java source files" [label="JCodeModel.build()"];
         model -> outline [label="BeanGenerator.generate()"];

         edge [style=dotted,label="associate"]
         outline -> codeModel;
         outline -> model;
       }
 * }
 *
 * <h2>Overview</h2>
 * <p>
 * XJC consists of the following major components.
 * <dl>
 *  <dt>{@link com.sun.tools.xjc.reader Schema reader}
 *  <dd>
 *   Schema readers read XML Schema documents (or DTD, RELAX NG, ...)
 *   and builds a model.
 *
 *  <dt>{@link com.sun.tools.xjc.model Model}
 *  <dd>
 *   Model represents the 'blueprint' of the code to be generated.
 *   Model talks in terms of higher level constructs like 'class' and 'property'
 *   without getting too much into the details of the Java source code.
 *
 *  <dt>{@link com.sun.tools.xjc.generator Code generator}
 *  <dd>
 *   Code generators use a model as an input and builds Java code AST
 *   into CodeModel. It also produces an {@link com.sun.tools.xjc.outline.Outline} which captures
 *   this work.
 *
 *  <dt>{@link com.sun.tools.xjc.outline.Outline Outline}
 *  <dd>
 *   Outline can be thought as a series of links between a model
 *   and CodeModel.
 * </dl>
 *
 * {@DotDiagram
 *   digraph G {
 *      rankdir = LR;
 *      schema -> reader -> model -> backend -> outline;
 *   }
 * }
 *
 * @ArchitectureDocument
 */
package com.sun.tools.xjc;
