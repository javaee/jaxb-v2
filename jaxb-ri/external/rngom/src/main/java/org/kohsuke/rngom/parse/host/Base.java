package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.om.Location;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class Base {
    protected AnnotationsHost cast( Annotations ann ) {
        if(ann==null)
            return nullAnnotations;
        else
            return (AnnotationsHost)ann;
    }
    
    protected LocationHost cast( Location loc ) {
        if(loc==null)
            return nullLocation;
        else
            return (LocationHost)loc;
    }
    
    private static final AnnotationsHost nullAnnotations = new AnnotationsHost(null,null);
    private static final LocationHost nullLocation = new LocationHost(null,null);
}
