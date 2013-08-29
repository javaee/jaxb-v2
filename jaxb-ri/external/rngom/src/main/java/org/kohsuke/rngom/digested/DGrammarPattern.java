package org.kohsuke.rngom.digested;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * &lt;grammar> pattern, which is a collection of named patterns.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DGrammarPattern extends DPattern implements Iterable<DDefine> {
    private final Map<String,DDefine> patterns = new HashMap<String,DDefine>();

    DPattern start;

    /**
     * Gets the start pattern.
     */
    public DPattern getStart() {
        return start;
    }

    /**
     * Gets the named pattern by its name.
     *
     * @return
     *      null if not found.
     */
    public DDefine get( String name ) {
        return patterns.get(name);
    }

    DDefine getOrAdd( String name ) {
        if(patterns.containsKey(name)) {
            return get(name);
        } else {
            DDefine d = new DDefine(name);
            patterns.put(name,d);
            return d;
        }
    }

    /**
     * Iterates all the {@link DDefine}s in this grammar.
     */
    public Iterator<DDefine> iterator() {
        return patterns.values().iterator();
    }

    public boolean isNullable() {
        return start.isNullable();
    }

    public <V> V accept( DPatternVisitor<V> visitor ) {
        return visitor.onGrammar(this);
    }
}
