package com.sun.tools.xjc.reader.dtd;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class Term {
    abstract void normalize( List<Block> r, boolean optional );

    abstract void addAllElements(Block b);

    abstract boolean isOptional();

    abstract boolean isRepeated();

    /**
     * Represents empty term.
     * <p>
     * This special term is only used to represent #PCDATA-only content model.
     */
    static final Term EMPTY = new Term() {
        void normalize(List<Block> r, boolean optional) {
        }

        void addAllElements(Block b) {
        }

        boolean isOptional() {
            return false;
        }

        boolean isRepeated() {
            return false;
        }
    };
}
