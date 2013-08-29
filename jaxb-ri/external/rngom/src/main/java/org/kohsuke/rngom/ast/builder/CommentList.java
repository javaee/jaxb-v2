package org.kohsuke.rngom.ast.builder;

import org.kohsuke.rngom.ast.om.Location;


public interface CommentList<L extends Location> {
  void addComment(String value, L loc) throws BuildException;
}
