package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.Location;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class LocationHost implements Location {
    final Location lhs;
    final Location rhs;
    
    LocationHost( Location lhs, Location rhs ) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}
