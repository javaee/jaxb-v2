package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class IncludeHost extends GrammarSectionHost implements Include {

    private final Include lhs;
    private final Include rhs;
    
    IncludeHost(Include lhs, Include rhs) {
        super(lhs, rhs);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void endInclude(Parseable current, String uri, String ns, Location _loc, Annotations _anno) throws BuildException, IllegalSchemaException {
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        lhs.endInclude( current, uri, ns, loc.lhs, anno.lhs );
        rhs.endInclude( current, uri, ns, loc.rhs, anno.rhs );
    }
}
