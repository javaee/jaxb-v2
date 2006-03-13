package com.sun.tools.xjc.reader.gbind;

/**
 * Source node of a graph.
 * @author Kohsuke Kawaguchi
 */
public final class SourceNode extends Element {
    public String toString() {
        return "#source";
    }

    @Override
    boolean isSource() {
        return true;
    }
}
