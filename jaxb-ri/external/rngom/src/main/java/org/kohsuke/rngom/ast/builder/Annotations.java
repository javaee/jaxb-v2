package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;


/**
 * Includes attributes and child elements before any RELAX NG element.
 */
public interface Annotations<
    E extends ParsedElementAnnotation,
    L extends Location,
    CL extends CommentList<L>> {

    /**
     * Called for an attribute annotation.
     */
    void addAttribute(String ns, String localName, String prefix, String value, L loc) throws BuildException;

    /**
     * Called for a child element annotation.
     */
    void addElement(E ea) throws BuildException;

    /**
     * Adds comments following the last initial child element annotation.
     */
    void addComment(CL comments) throws BuildException;

    void addLeadingComment(CL comments) throws BuildException;
}
