package com.sun.tools.txw2.builder.relaxng;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.util.LocatorImpl;

/**
 * @author Kohsuke Kawaguchi
 */
final class AnnotationsImpl implements Annotations<ParsedElementAnnotation,LocatorImpl,CommentListImpl> {
    public void addAttribute(String ns, String localName, String prefix, String value, LocatorImpl locator) throws BuildException {
    }

    public void addElement(ParsedElementAnnotation parsedElementAnnotation) throws BuildException {
    }

    public void addComment(CommentListImpl commentList) throws BuildException {
    }

    public void addLeadingComment(CommentListImpl commentList) throws BuildException {
    }
}
