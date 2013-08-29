package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.builder.GrammarSection;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class GrammarSectionHost extends Base implements GrammarSection {
    private final GrammarSection lhs;
    private final GrammarSection rhs;
    
    GrammarSectionHost( GrammarSection lhs, GrammarSection rhs ) {
        this.lhs = lhs;
        this.rhs = rhs;
        if(lhs==null || rhs==null)
            throw new IllegalArgumentException();
    }
    
    public void define(String name, Combine combine, ParsedPattern _pattern,
        Location _loc, Annotations _anno) throws BuildException {
        ParsedPatternHost pattern = (ParsedPatternHost) _pattern;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        lhs.define(name, combine, pattern.lhs, loc.lhs, anno.lhs);
        rhs.define(name, combine, pattern.rhs, loc.rhs, anno.rhs);
    }
    
    public Div makeDiv() {
        return new DivHost( lhs.makeDiv(), rhs.makeDiv() );
    }
    
    public Include makeInclude() {
        Include l = lhs.makeInclude();
        if(l==null) return null;
        return new IncludeHost( l, rhs.makeInclude() );
    }
    
    public void topLevelAnnotation(ParsedElementAnnotation _ea) throws BuildException {
        ParsedElementAnnotationHost ea = (ParsedElementAnnotationHost) _ea;
        lhs.topLevelAnnotation(ea==null?null:ea.lhs);
        rhs.topLevelAnnotation(ea==null?null:ea.rhs);
    }
    
    public void topLevelComment(CommentList _comments) throws BuildException {
        CommentListHost comments = (CommentListHost) _comments;

        lhs.topLevelComment(comments==null?null:comments.lhs);
        rhs.topLevelComment(comments==null?null:comments.rhs);
    }
}
