package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.builder.NameClassBuilder;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedNameClass;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Context;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class SchemaBuilderHost extends Base implements SchemaBuilder {
    final SchemaBuilder lhs;
    final SchemaBuilder rhs;
    
    public SchemaBuilderHost( SchemaBuilder lhs, SchemaBuilder rhs ) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
    
    public ParsedPattern annotate(ParsedPattern _p, Annotations _anno)
        throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        AnnotationsHost a = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.annotate(p.lhs, a.lhs),
            rhs.annotate(p.lhs, a.lhs) );
    }
    
    public ParsedPattern annotateAfter(ParsedPattern _p,
        ParsedElementAnnotation _e) throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        ParsedElementAnnotationHost e = (ParsedElementAnnotationHost) _e;
        return new ParsedPatternHost(
            lhs.annotateAfter(p.lhs, e.lhs),
            rhs.annotateAfter(p.rhs, e.rhs));
    }
    
    public ParsedPattern commentAfter(ParsedPattern _p, CommentList _comments)
        throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        CommentListHost comments = (CommentListHost) _comments;
        
        return new ParsedPatternHost(
            lhs.commentAfter(p.lhs, comments==null?null:comments.lhs),
            rhs.commentAfter(p.rhs, comments==null?null:comments.rhs));
    }
    
    public ParsedPattern expandPattern(ParsedPattern _p) throws BuildException, IllegalSchemaException {
        ParsedPatternHost p = (ParsedPatternHost) _p;
        return new ParsedPatternHost(
            lhs.expandPattern(p.lhs),
            rhs.expandPattern(p.rhs));
    }
    
    public NameClassBuilder getNameClassBuilder() throws BuildException {
        return new NameClassBuilderHost( lhs.getNameClassBuilder(), rhs.getNameClassBuilder() );
    }
    
    public Annotations makeAnnotations(CommentList _comments, Context context) {
        CommentListHost comments = (CommentListHost) _comments;
        Annotations l = lhs.makeAnnotations((comments!=null)?comments.lhs:null, context);
        Annotations r = rhs.makeAnnotations((comments!=null)?comments.rhs:null, context);
        if(l==null || r==null)
            throw new IllegalArgumentException("annotations cannot be null");
        return new AnnotationsHost(l,r);
    }
    
    public ParsedPattern makeAttribute(ParsedNameClass _nc, ParsedPattern _p,
        Location _loc, Annotations _anno) throws BuildException {
        
        ParsedNameClassHost nc = (ParsedNameClassHost) _nc;
        ParsedPatternHost p = (ParsedPatternHost) _p;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeAttribute(nc.lhs, p.lhs, loc.lhs, anno.lhs),
            rhs.makeAttribute(nc.rhs, p.rhs, loc.rhs, anno.rhs));
    }
    
    public ParsedPattern makeChoice(List patterns,
        Location _loc, Annotations _anno) throws BuildException {

        List<ParsedPattern> lp = new ArrayList<ParsedPattern>();
        List<ParsedPattern> rp = new ArrayList<ParsedPattern>();
        for( int i=0; i<patterns.size(); i++ ) {
            lp.add( ((ParsedPatternHost)patterns.get(i)).lhs);
            rp.add( ((ParsedPatternHost)patterns.get(i)).rhs);
        }
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeChoice(lp, loc.lhs, anno.lhs),
            rhs.makeChoice(rp, loc.rhs, anno.rhs));
    }
    
    public CommentList makeCommentList() {
        return new CommentListHost(
            lhs.makeCommentList(),
            rhs.makeCommentList() );
    }
    
    public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary,
        String type, Location _loc) throws BuildException {
        LocationHost loc = cast(_loc);
        
        return new DataPatternBuilderHost(
            lhs.makeDataPatternBuilder(datatypeLibrary, type, loc.lhs),
            rhs.makeDataPatternBuilder(datatypeLibrary, type, loc.rhs) );
    }
    
    public ParsedPattern makeElement(ParsedNameClass _nc, ParsedPattern _p,
        Location _loc, Annotations _anno) throws BuildException {
        
        ParsedNameClassHost nc = (ParsedNameClassHost) _nc;
        ParsedPatternHost p = (ParsedPatternHost) _p;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeElement(nc.lhs, p.lhs, loc.lhs, anno.lhs),
            rhs.makeElement(nc.rhs, p.rhs, loc.rhs, anno.rhs));
    }
    
    public ElementAnnotationBuilder makeElementAnnotationBuilder(String ns,
        String localName, String prefix, Location _loc, CommentList _comments,
        Context context) {
        LocationHost loc = cast(_loc);
        CommentListHost comments = (CommentListHost) _comments;
        
        return new ElementAnnotationBuilderHost(
            lhs.makeElementAnnotationBuilder(ns, localName, prefix, loc.lhs, comments==null?null:comments.lhs, context),
            rhs.makeElementAnnotationBuilder(ns, localName, prefix, loc.rhs, comments==null?null:comments.rhs, context) );
    }
    
    public ParsedPattern makeEmpty(Location _loc, Annotations _anno) {
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeEmpty(loc.lhs, anno.lhs),
            rhs.makeEmpty(loc.rhs, anno.rhs));
    }
    
    public ParsedPattern makeErrorPattern() {
        return new ParsedPatternHost(
            lhs.makeErrorPattern(),
            rhs.makeErrorPattern() );
    }
    
    public ParsedPattern makeExternalRef(Parseable current, String uri,
        String ns, Scope _scope, Location _loc, Annotations _anno)
        throws BuildException, IllegalSchemaException {
        
        ScopeHost scope = (ScopeHost) _scope;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeExternalRef(current, uri, ns, scope.lhs, loc.lhs, anno.lhs),
            rhs.makeExternalRef(current, uri, ns, scope.rhs, loc.rhs, anno.rhs) );
    }
    
    public Grammar makeGrammar(Scope _parent) {
        ScopeHost parent = (ScopeHost) _parent;

        return new GrammarHost(
            lhs.makeGrammar((parent!=null)?parent.lhs:null),
            rhs.makeGrammar((parent!=null)?parent.rhs:null) );
    }

    public ParsedPattern makeGroup(List patterns,
        Location _loc, Annotations _anno) throws BuildException {

        List<ParsedPattern> lp = new ArrayList<ParsedPattern>();
        List<ParsedPattern> rp = new ArrayList<ParsedPattern>();
        for( int i=0; i<patterns.size(); i++ ) {
            lp.add( ((ParsedPatternHost)patterns.get(i)).lhs);
            rp.add( ((ParsedPatternHost)patterns.get(i)).rhs);
        }
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);

        return new ParsedPatternHost(
            lhs.makeGroup(lp, loc.lhs, anno.lhs),
            rhs.makeGroup(rp, loc.rhs, anno.rhs));
    }

    public ParsedPattern makeInterleave(List patterns,
        Location _loc, Annotations _anno) throws BuildException {

        List<ParsedPattern> lp = new ArrayList<ParsedPattern>();
        List<ParsedPattern> rp = new ArrayList<ParsedPattern>();
        for( int i=0; i<patterns.size(); i++ ) {
            lp.add( ((ParsedPatternHost)patterns.get(i)).lhs);
            rp.add( ((ParsedPatternHost)patterns.get(i)).rhs);
        }
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);

        return new ParsedPatternHost(
            lhs.makeInterleave(lp, loc.lhs, anno.lhs),
            rhs.makeInterleave(rp, loc.rhs, anno.rhs));
    }
    
    public ParsedPattern makeList(ParsedPattern _p, Location _loc,
        Annotations _anno) throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeList(p.lhs, loc.lhs, anno.lhs),
            rhs.makeList(p.rhs, loc.rhs, anno.rhs));
    }
    
    public Location makeLocation(String systemId, int lineNumber,
        int columnNumber) {
        return new LocationHost(
            lhs.makeLocation(systemId, lineNumber, columnNumber),
            rhs.makeLocation(systemId, lineNumber, columnNumber));
    }
    
    public ParsedPattern makeMixed(ParsedPattern _p, Location _loc,
        Annotations _anno) throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeMixed(p.lhs, loc.lhs, anno.lhs),
            rhs.makeMixed(p.rhs, loc.rhs, anno.rhs));
    }
    
    public ParsedPattern makeNotAllowed(Location _loc, Annotations _anno) {
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeNotAllowed(loc.lhs, anno.lhs),
            rhs.makeNotAllowed(loc.rhs, anno.rhs));
    }

    public ParsedPattern makeOneOrMore(ParsedPattern _p, Location _loc,
        Annotations _anno) throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeOneOrMore(p.lhs, loc.lhs, anno.lhs),
            rhs.makeOneOrMore(p.rhs, loc.rhs, anno.rhs));
    }
    
    public ParsedPattern makeZeroOrMore(ParsedPattern _p, Location _loc,
        Annotations _anno) throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeZeroOrMore(p.lhs, loc.lhs, anno.lhs),
            rhs.makeZeroOrMore(p.rhs, loc.rhs, anno.rhs));
    }

    public ParsedPattern makeOptional(ParsedPattern _p, Location _loc,
        Annotations _anno) throws BuildException {
        
        ParsedPatternHost p = (ParsedPatternHost) _p;
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeOptional(p.lhs, loc.lhs, anno.lhs),
            rhs.makeOptional(p.rhs, loc.rhs, anno.rhs));
    }
    
    public ParsedPattern makeText(Location _loc, Annotations _anno) {
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeText(loc.lhs, anno.lhs),
            rhs.makeText(loc.rhs, anno.rhs));
    }
    
    public ParsedPattern makeValue(String datatypeLibrary, String type,
        String value, Context c, String ns, Location _loc, Annotations _anno)
        throws BuildException {
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.makeValue(datatypeLibrary,type,value,c,ns,loc.lhs,anno.lhs),
            rhs.makeValue(datatypeLibrary,type,value,c,ns,loc.rhs,anno.rhs));
    }
    
    public boolean usesComments() {
        return lhs.usesComments() || rhs.usesComments();
    }
}
