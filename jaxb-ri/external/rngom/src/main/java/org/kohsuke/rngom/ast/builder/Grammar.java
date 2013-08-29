package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

/**
 * {@link Scope} for &lt;grammar> element that serves as a container
 * of &lt;define>s.
 */
public interface Grammar<
    P extends ParsedPattern,
    E extends ParsedElementAnnotation,
    L extends Location,
    A extends Annotations<E,L,CL>,
    CL extends CommentList<L>> extends GrammarSection<P,E,L,A,CL>, Scope<P,E,L,A,CL> {
    P endGrammar(L loc, A anno) throws BuildException;
}
