/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.txw2.builder.relaxng;

import com.sun.tools.txw2.model.Define;
import com.sun.tools.txw2.model.Grammar;
import com.sun.tools.txw2.model.Leaf;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.GrammarSection;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.util.LocatorImpl;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class GrammarSectionImpl implements GrammarSection<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> {

    protected final Scope<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> parent;

    protected final Grammar grammar;

    GrammarSectionImpl(
        Scope<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> scope,
        Grammar grammar ) {
        this.parent = scope;
        this.grammar = grammar;
    }

    public void topLevelAnnotation(ParsedElementAnnotation parsedElementAnnotation) throws BuildException {
    }

    public void topLevelComment(CommentListImpl commentList) throws BuildException {
    }

    public Div<Leaf, ParsedElementAnnotation, LocatorImpl, AnnotationsImpl, CommentListImpl> makeDiv() {
        return new DivImpl(parent,grammar);
    }

    public Include<Leaf, ParsedElementAnnotation, LocatorImpl, AnnotationsImpl, CommentListImpl> makeInclude() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void define(String name, Combine combine, Leaf leaf, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        Define def = grammar.get(name);
        def.location = locator;

        if(combine==null || def.leaf==null) {
            def.leaf = leaf;
        } else {
            def.leaf.merge(leaf);
        }
    }
}
