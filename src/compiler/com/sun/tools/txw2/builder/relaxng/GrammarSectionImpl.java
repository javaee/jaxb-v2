/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
