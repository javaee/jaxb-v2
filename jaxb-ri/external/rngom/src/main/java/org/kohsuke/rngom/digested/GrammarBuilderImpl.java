package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.ast.util.LocatorImpl;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class GrammarBuilderImpl implements Grammar, Div {

    protected final DGrammarPattern grammar;

    protected final Scope parent;

    protected final DSchemaBuilderImpl sb;

    /**
     * Additional top-level element annotations.
     * Can be null.
     */
    private List<Element> additionalElementAnnotations;

    public GrammarBuilderImpl(DGrammarPattern p, Scope parent, DSchemaBuilderImpl sb) {
        this.grammar = p;
        this.parent = parent;
        this.sb = sb;
    }

    public ParsedPattern endGrammar(Location loc, Annotations anno) throws BuildException {
        if(anno!=null)
            grammar.annotation = ((Annotation)anno).getResult();
        if(additionalElementAnnotations!=null) {
            if(grammar.annotation==null)
                grammar.annotation = new DAnnotation();
            grammar.annotation.contents.addAll(additionalElementAnnotations);
        }
        return grammar;
    }

    public void endDiv(Location loc, Annotations anno) throws BuildException {
    }

    public void define(String name, Combine combine, ParsedPattern pattern, Location loc, Annotations anno) throws BuildException {
        if(name==START)
            grammar.start = (DPattern)pattern;
        else {
            // TODO: handle combine
            DDefine d = grammar.getOrAdd(name);
            d.setPattern( (DPattern) pattern );
            if(anno!=null)
                d.annotation = ((Annotation)anno).getResult();
        }
    }

    public void topLevelAnnotation(ParsedElementAnnotation ea) throws BuildException {
        if(additionalElementAnnotations==null)
            additionalElementAnnotations = new ArrayList<Element>();
        additionalElementAnnotations.add(((ElementWrapper)ea).element);
    }

    public void topLevelComment(CommentList comments) throws BuildException {
    }

    public Div makeDiv() {
        return this;
    }

    public Include makeInclude() {
        return new IncludeImpl(grammar,parent,sb);
    }

    public ParsedPattern makeParentRef(String name, Location loc, Annotations anno) throws BuildException {
        return parent.makeRef(name,loc,anno);
    }

    public ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException {
        return DSchemaBuilderImpl.wrap( new DRefPattern(grammar.getOrAdd(name)), (LocatorImpl)loc, (Annotation)anno );
    }
}
