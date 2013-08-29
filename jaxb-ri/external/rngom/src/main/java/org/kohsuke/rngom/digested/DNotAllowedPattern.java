package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DNotAllowedPattern extends DPattern {
    public boolean isNullable() {
        return false;
    }
    public Object accept( DPatternVisitor visitor ) {
        return visitor.onNotAllowed(this);
    }
}
