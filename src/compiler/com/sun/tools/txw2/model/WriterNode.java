package com.sun.tools.txw2.model;

import org.xml.sax.Locator;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class WriterNode extends Node {
    /**
     * If this node is the sole child of a pattern block,
     * this field points to its name.
     *
     * <p>
     * When the element names are in conflict, this can be used.
     */
    protected String alternativeName;

    public WriterNode(Locator location, Leaf leaf) {
        super(location, leaf);
    }

    /**
     * Declares the class without its contents.
     *
     * The first step of the code generation.
     */
    abstract void declare(NodeSet nset);

    /**
     * Generates the contents.
     */
    abstract void generate(NodeSet nset);

    /**
     * Prepares for the code generation.
     */
    void prepare(NodeSet nset) {}
}
