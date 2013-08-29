package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.nc.NameClass;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DElementPattern extends DXmlTokenPattern {
    public DElementPattern(NameClass name) {
        super(name);
    }

    public Object accept( DPatternVisitor visitor ) {
        return visitor.onElement(this);
    }
}
