package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.*;


public interface DataPatternBuilder<
    P extends ParsedPattern,
    E extends ParsedElementAnnotation,
    L extends Location,
    A extends Annotations<E,L,CL>,
    CL extends CommentList<L>> {

  void addParam(String name, String value, Context context, String ns, L loc, A anno) throws BuildException;
  void annotation(E ea);
  P makePattern(L loc, A anno) throws BuildException;
  P makePattern(P except, L loc, A anno) throws BuildException;
}
