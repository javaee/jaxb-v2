package com.sun.tools.txw2.model;

import org.kohsuke.rngom.ast.builder.GrammarSection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Grammar {
    private final Map<String,Define> patterns = new HashMap<String,Define>();

    public Define get(String name) {
        Define def = patterns.get(name);
        if(def==null)
            patterns.put(name,def=new Define(this,name));
        return def;
    }

    /**
     * The name for the start pattern
     */
    public static final String START = GrammarSection.START;
}
