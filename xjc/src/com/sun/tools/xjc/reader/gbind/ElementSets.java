package com.sun.tools.xjc.reader.gbind;

import java.util.HashSet;

/**
 * Factory methods for {@link ElementSet}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ElementSets {
    /**
     * Returns an union of two {@link ElementSet}s.
     *
     * This method performs better if lhs is bigger than rhs
     */
    public static ElementSet union(ElementSet lhs, ElementSet rhs) {
        if(lhs.contains(rhs))
            return lhs;
        if(lhs==ElementSet.EMPTY_SET)
            return rhs;
        if(rhs==ElementSet.EMPTY_SET)
            return lhs;
        return new MultiValueSet(lhs,rhs);
    }

    /**
     * {@link ElementSet} that has multiple {@link Element}s in it.
     *
     * This isn't particularly efficient or anything, but it will do for now.
     */
    private static final class MultiValueSet extends HashSet<Element> implements ElementSet {
        public MultiValueSet(ElementSet lhs, ElementSet rhs) {
            addAll(lhs);
            addAll(rhs);
            // not that anything will break with size==1 MultiValueSet,
            // but it does suggest that we are missing an easy optimization
            assert size()>1;
        }

        private void addAll(ElementSet lhs) {
            if(lhs instanceof MultiValueSet) {
                super.addAll((MultiValueSet)lhs);
            } else {
                for (Element e : lhs)
                    add(e);
            }
        }

        public boolean contains(ElementSet rhs) {
            // this isn't complete but sound
            return super.contains(rhs) || rhs==ElementSet.EMPTY_SET;
        }

        public void addNext(Element element) {
            for (Element e : this)
                e.addNext(element);
        }
    }
}
