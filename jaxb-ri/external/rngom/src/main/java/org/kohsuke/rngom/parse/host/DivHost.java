package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.Div;
import org.kohsuke.rngom.ast.om.Location;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DivHost extends GrammarSectionHost implements Div {
    private final Div lhs;
    private final Div rhs;
    
    DivHost(Div lhs, Div rhs) {
        super(lhs, rhs);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void endDiv(Location _loc, Annotations _anno) throws BuildException {
        LocationHost loc = cast(_loc);
        AnnotationsHost anno = cast(_anno);
        
        lhs.endDiv( loc.lhs, anno.lhs );
        rhs.endDiv( loc.rhs, anno.rhs );
    }
    
}
