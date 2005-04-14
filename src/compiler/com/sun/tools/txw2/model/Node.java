package com.sun.tools.txw2.model;

import org.xml.sax.Locator;

import java.util.Iterator;

/**
 * {@link Node} is a {@link Leaf} that has children.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Node extends Leaf implements Iterable<Leaf> {

    /**
     * Children of this node.
     */
    public Leaf leaf;

    protected Node(Locator location, Leaf leaf) {
        super(location);
        this.leaf = leaf;
    }

    /**
     * Iterates all the children.
     */
    public final Iterator<Leaf> iterator() {
        return new CycleIterator(leaf);
    }

    /**
     * Returns true if this node has only one child node.
     */
    public final boolean hasOneChild() {
        return leaf==leaf.getNext();
    }

}
