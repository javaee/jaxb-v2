package com.sun.tools.txw2.model;

import com.sun.codemodel.JType;

/**
 * Implemented by {@link Leaf}s that map to PCDATA in XML.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Text {
    /**
     * Obtains the Java class of this {@link Text}.
     */
    JType getDatatype(NodeSet nset);
}
