package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Grammar;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedPattern;

/**
 * Wraps {@link Grammar} and provides error checking.
 * 
 * <p>
 * The following errors are checked by this host:
 * 
 * <ol>
 *  <li>referenced to undefined patterns.
 * </ol>
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class GrammarHost extends ScopeHost implements Grammar {
    final Grammar lhs;
    final Grammar rhs;

    public GrammarHost(Grammar lhs,Grammar rhs) {
        super(lhs,rhs);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public ParsedPattern endGrammar(Location _loc, Annotations _anno) throws BuildException {
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        return new ParsedPatternHost(
            lhs.endGrammar(loc.lhs, anno.lhs),
            rhs.endGrammar(loc.rhs, anno.rhs));
    }
}
