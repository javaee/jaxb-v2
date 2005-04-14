package com.sun.tools.txw2.builder;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.ElementAnnotationBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

/**
 * @author Kohsuke Kawaguchi
 */
final class ElementAnnotationBuilderImpl implements ElementAnnotationBuilder {
    public void addText(String value, Location location, CommentList commentList) throws BuildException {
    }

    public ParsedElementAnnotation makeElementAnnotation() throws BuildException {
        return null;
    }

    public void addAttribute(String ns, String localName, String prefix, String value, Location location) throws BuildException {
    }

    public void addElement(ParsedElementAnnotation parsedElementAnnotation) throws BuildException {
    }

    public void addComment(CommentList commentList) throws BuildException {
    }

    public void addLeadingComment(CommentList commentList) throws BuildException {
    }
}
