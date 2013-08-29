package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;

public interface Include<
    P extends ParsedPattern,
    E extends ParsedElementAnnotation,
    L extends Location,
    A extends Annotations<E,L,CL>,
    CL extends CommentList<L>> extends GrammarSection<P,E,L,A,CL> {
    /**
     * @param current
     *      The current document we are parsing.
     *      This is the document that contains an include.
     */
  void endInclude(Parseable current, String uri, String ns,
                  L loc, A anno) throws BuildException, IllegalSchemaException;
}
