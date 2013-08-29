package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.nc.NameClass;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DAttributePattern extends DXmlTokenPattern {
    public DAttributePattern(NameClass name) {
        super(name);
    }
    public Object accept( DPatternVisitor visitor ) {
        return visitor.onAttribute(this);
    }
}
