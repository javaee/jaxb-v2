package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DInterleavePattern extends DContainerPattern {
    public boolean isNullable() {
        for( DPattern p=firstChild(); p!=null; p=p.next )
            if(!p.isNullable())
                return false;
        return true;
    }
    public Object accept( DPatternVisitor visitor ) {
        return visitor.onInterleave(this);
    }
}
