package org.kohsuke.rngom.nc;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.NameClassBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

import java.util.List;


/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class NameClassBuilderImpl<
    E extends ParsedElementAnnotation,
    L extends Location,
    A extends Annotations<E,L,CL>,
    CL extends CommentList<L>> implements NameClassBuilder<NameClass,E,L,A,CL> {
    
    public NameClass makeChoice(List<NameClass> nameClasses, L loc, A anno) {
      NameClass result = nameClasses.get(0);
      for (int i = 1; i < nameClasses.size(); i++)
        result = new ChoiceNameClass(result, nameClasses.get(i));
      return result;
    }

    public NameClass makeName(String ns, String localName, String prefix, L loc, A anno) {
      return new SimpleNameClass(ns, localName);
    }

    public NameClass makeNsName(String ns, L loc, A anno) {
      return new NsNameClass(ns);
    }

    public NameClass makeNsName(String ns, NameClass except, L loc, A anno) {
      return new NsNameExceptNameClass(ns, except);
    }

    public NameClass makeAnyName(L loc, A anno) {
      return NameClass.ANY;
    }

    public NameClass makeAnyName(NameClass except, L loc, A anno) {
      return new AnyNameExceptNameClass(except);
    }

    public NameClass makeErrorNameClass() {
        return NameClass.NULL;
    }
    
    public NameClass annotate(NameClass nc, A anno) throws BuildException {
      return nc;
    }
    
    public NameClass annotateAfter(NameClass nc, E e) throws BuildException {
      return nc;
    }
    
    public NameClass commentAfter(NameClass nc, CL comments) throws BuildException {
      return nc;
    }

}
