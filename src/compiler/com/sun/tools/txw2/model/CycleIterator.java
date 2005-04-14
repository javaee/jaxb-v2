package com.sun.tools.txw2.model;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
final class CycleIterator implements Iterator<Leaf> {
    private Leaf start;
    private Leaf current;
    private boolean hasNext = true;

    public CycleIterator(Leaf start) {
        assert start!=null;
        this.start = start;
        this.current = start;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public Leaf next() {
        Leaf last = current;
        current = current.getNext();
        if(current==start)
            hasNext = false;

        return last;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
