package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DEmptyPattern extends DPattern {
    public boolean isNullable() {
        return true;
    }
    public Object accept( DPatternVisitor visitor ) {
        return visitor.onEmpty(this);
    }
}
