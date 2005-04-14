package com.sun.tools.txw2.builder;

import com.sun.tools.txw2.model.Grammar;
import com.sun.tools.txw2.model.Leaf;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.util.LocatorImpl;

/**
 * @author Kohsuke Kawaguchi
 */
class DivImpl
    extends GrammarSectionImpl
    implements Div<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> {

    DivImpl(Scope<Leaf,ParsedElementAnnotation,LocatorImpl,AnnotationsImpl,CommentListImpl> parent, Grammar grammar) {
        super(parent,grammar);
    }

    public void endDiv(LocatorImpl locator, AnnotationsImpl annotations) {
    }
}
