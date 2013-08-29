package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

public interface Scope<
    P extends ParsedPattern,
    E extends ParsedElementAnnotation,
    L extends Location,
    A extends Annotations<E,L,CL>,
    CL extends CommentList<L> > extends GrammarSection<P,E,L,A,CL> {
    P makeParentRef(String name, L loc, A anno) throws BuildException;
    P makeRef(String name, L loc, A anno) throws BuildException;
}
