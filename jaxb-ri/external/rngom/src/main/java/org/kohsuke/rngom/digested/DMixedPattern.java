package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DMixedPattern extends DUnaryPattern {
    public boolean isNullable() {
        return getChild().isNullable();
    }
    public Object accept( DPatternVisitor visitor ) {
        return visitor.onMixed(this);
    }
}
