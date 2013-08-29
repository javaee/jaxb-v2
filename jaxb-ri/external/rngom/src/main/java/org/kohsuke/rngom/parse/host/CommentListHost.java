package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.CommentList;
import org.kohsuke.rngom.ast.om.Location;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class CommentListHost extends Base implements CommentList {

    final CommentList lhs;
    final CommentList rhs;

    CommentListHost(CommentList lhs, CommentList rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
    
    public void addComment(String value, Location _loc) throws BuildException {
        LocationHost loc = cast(_loc);
        if(lhs!=null)
            lhs.addComment(value,loc.lhs);
        if(rhs!=null)
            rhs.addComment(value,loc.rhs);
    }
}
