package com.sun.xml.bind.taglets;




/**
 * Implements a @SequenceDiagram tag.
 *
 * @author Kohsuke Kawaguchi
 */
public class SequenceDiagramTag extends ImageGeneratingTag {

    public SequenceDiagramTag() {
        super("http://kohsuke.sfbay/sequence-diagram/Build", "sequence-diagram");
    }

    public String getName() {
        return "SequenceDiagram";
    }
}
