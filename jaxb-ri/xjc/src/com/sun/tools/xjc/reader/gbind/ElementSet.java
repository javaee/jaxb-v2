package com.sun.tools.xjc.reader.gbind;

import java.util.Collections;
import java.util.Iterator;

/**
 * A set over a list of {@link Element}.
 *
 * @author Kohsuke Kawaguchi
 */
interface ElementSet extends Iterable<Element> {
    /**
     * For each element in this set, adds an edge to the given element.
     */
    void addNext(Element element);

    public static final ElementSet EMPTY_SET = new ElementSet() {
        public void addNext(Element element) {
            // noop
        }

        public boolean contains(ElementSet element) {
            return this==element;
        }

        public Iterator<Element> iterator() {
            return Collections.<Element>emptySet().iterator();
        }
    };

    /**
     * Doesn't have to be strict. Used just for optimization.
     */
    boolean contains(ElementSet rhs);
}
