package org.kohsuke.rngom.digested;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class DUnaryPattern extends DPattern {
    private DPattern child;

    public DPattern getChild() {
        return child;
    }

    public void setChild(DPattern child) {
        this.child = child;
    }
}
