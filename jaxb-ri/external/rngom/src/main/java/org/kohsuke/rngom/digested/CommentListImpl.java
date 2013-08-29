package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.util.LocatorImpl;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
final class CommentListImpl implements CommentList<LocatorImpl> {
    public void addComment(String value, LocatorImpl loc) throws BuildException {
        // TODO
    }
}
