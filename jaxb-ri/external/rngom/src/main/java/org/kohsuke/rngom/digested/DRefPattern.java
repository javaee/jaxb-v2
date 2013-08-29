package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DRefPattern extends DPattern {
    private final DDefine target;

    public DRefPattern(DDefine target) {
        this.target = target;
    }

    public boolean isNullable() {
        return target.isNullable();
    }

    /**
     * Gets the {@link DDefine} that this block refers to.
     */
    public DDefine getTarget() {
        return target;
    }

    /**
     * Gets the name of the target.
     */
    public String getName() {
        return target.getName();
    }

    public Object accept( DPatternVisitor visitor ) {
        return visitor.onRef(this);
    }
}
