package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DGroupPattern extends DContainerPattern {
    public boolean isNullable() {
        for( DPattern p=firstChild(); p!=null; p=p.next )
            if(!p.isNullable())
                return false;
        return true;
    }
    public <V> V accept( DPatternVisitor<V> visitor ) {
        return visitor.onGroup(this);
    }
}
