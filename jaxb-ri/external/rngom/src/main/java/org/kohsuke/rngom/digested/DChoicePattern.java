package org.kohsuke.rngom.digested;

/**
 * &lt;choice> pattern.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DChoicePattern extends DContainerPattern {
    public boolean isNullable() {
        for( DPattern p=firstChild(); p!=null; p=p.next )
            if(p.isNullable())
                return true;
        return false;
    }
    public <V> V accept( DPatternVisitor<V> visitor ) {
        return visitor.onChoice(this);
    }
}
