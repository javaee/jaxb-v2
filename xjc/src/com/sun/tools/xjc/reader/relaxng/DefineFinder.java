package com.sun.tools.xjc.reader.relaxng;

import java.util.HashSet;
import java.util.Set;

import org.kohsuke.rngom.digested.DDefine;
import org.kohsuke.rngom.digested.DGrammarPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DRefPattern;

/**
 * Recursively find all {@link DDefine}s in the grammar.
 *
 * @author Kohsuke Kawaguchi
 */
final class DefineFinder extends DPatternWalker {

    public final Set<DDefine> defs = new HashSet<DDefine>();

    public Void onGrammar(DGrammarPattern p) {
        for( DDefine def : p ) {
            defs.add(def);
            def.getPattern().accept(this);
        }

        return p.getStart().accept(this);
    }

    /**
     * We visit all {@link DDefine}s from {@link DGrammarPattern},
     * so no point in resolving refs.
     */
    public Void onRef(DRefPattern p) {
        return null;
    }
}
