package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DOptionalPattern extends DUnaryPattern {
    public boolean isNullable() {
        return true;
    }
    public Object accept( DPatternVisitor visitor ) {
        return visitor.onOptional(this);
    }
}
