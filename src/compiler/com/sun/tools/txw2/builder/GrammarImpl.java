package com.sun.tools.txw2.builder;

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
