package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Context;
import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class DataPatternBuilderImpl implements DataPatternBuilder {

    private final DDataPattern p;

    public DataPatternBuilderImpl(String datatypeLibrary, String type, Location loc) {
        p = new DDataPattern();
        p.location = (Locator)loc;
        p.datatypeLibrary = datatypeLibrary;
        p.type = type;
    }

    public void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno) throws BuildException {
        p.params.add(p.new Param(name,value,context.copy(),ns,loc,(Annotation)anno));
    }

    public void annotation(ParsedElementAnnotation ea) {
        // TODO
    }

    public ParsedPattern makePattern(Location loc, Annotations anno) throws BuildException {
        return makePattern(null,loc,anno);
    }

    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno) throws BuildException {
        p.except = (DPattern)except;
        if(anno!=null)
            p.annotation = ((Annotation)anno).getResult();
        return p;
    }
}
