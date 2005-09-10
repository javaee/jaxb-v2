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

import com.sun.tools.txw2.model.Leaf;
import com.sun.tools.txw2.model.Ref;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.util.LocatorImpl;

/**
 * @author Kohsuke Kawaguchi
 */
class GrammarImpl extends GrammarSectionImpl
    implements Grammar<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> {

    GrammarImpl(Scope<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> scope) {
        super(scope,new com.sun.tools.txw2.model.Grammar());
    }

    public Leaf endGrammar(LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return new Ref(locator,grammar,com.sun.tools.txw2.model.Grammar.START);
    }

    public Leaf makeParentRef(String name, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return parent.makeRef(name,locator,annotations);
    }

    public Leaf makeRef(String name, LocatorImpl locator, AnnotationsImpl annotations) throws BuildException {
        return new Ref(locator,grammar,name);
    }
}
