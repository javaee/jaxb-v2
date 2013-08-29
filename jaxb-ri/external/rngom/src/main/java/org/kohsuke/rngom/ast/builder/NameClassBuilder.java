package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedNameClass;

import java.util.List;


/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface NameClassBuilder<
    N extends ParsedNameClass,
    E extends ParsedElementAnnotation,
    L extends Location,
    A extends Annotations<E,L,CL>,
    CL extends CommentList<L> > {

    N annotate(N nc, A anno) throws BuildException;
    N annotateAfter(N nc, E e) throws BuildException;
    N commentAfter(N nc, CL comments) throws BuildException;
    N makeChoice(List<N> nameClasses, L loc, A anno);

// should be handled by parser - KK
//    static final String INHERIT_NS = new String("#inherit");

// similarly, xmlns:* attribute should be rejected by the parser -KK
    
    N makeName(String ns, String localName, String prefix, L loc, A anno);
    N makeNsName(String ns, L loc, A anno);
    /**
     * Caller must enforce constraints on except.
     */
    N makeNsName(String ns, N except, L loc, A anno);
    N makeAnyName(L loc, A anno);
    /**
     * Caller must enforce constraints on except.
     */
    N makeAnyName(N except, L loc, A anno);

    N makeErrorNameClass();
}
