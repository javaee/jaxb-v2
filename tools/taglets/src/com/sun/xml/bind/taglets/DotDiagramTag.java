package com.sun.xml.bind.taglets;



/**
 * Uses GraphViz/Dot to draw a directed diagram.
 *
 * @author Kohsuke Kawaguchi
 */
public class DotDiagramTag extends ImageGeneratingTag {

    public DotDiagramTag() {
        super("http://kohsuke.sfbay/graphviz-server/Build", "dot-diagram");
    }

    public String getName() {
        return "DotDiagram";
    }
}
