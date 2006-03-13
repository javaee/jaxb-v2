package com.sun.tools.xjc.reader.gbind;

/**
 * Sink node of a grpah.
 * @author Kohsuke Kawaguchi
 */
public final class SinkNode extends Element {
    public String toString() {
        return "#sink";
    }

    @Override
    boolean isSink() {
        return true;
    }
}
