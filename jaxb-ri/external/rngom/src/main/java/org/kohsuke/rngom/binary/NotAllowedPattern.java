package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class NotAllowedPattern extends Pattern {
  NotAllowedPattern() {
    super(false, EMPTY_CONTENT_TYPE, NOT_ALLOWED_HASH_CODE);
  }
  boolean isNotAllowed() {
    return true;
  }
  boolean samePattern(Pattern other) {
    // needs to work for UnexpandedNotAllowedPattern
    return other.getClass() == this.getClass();
  }
  public void accept(PatternVisitor visitor) {
    visitor.visitNotAllowed();
  }
  public Object apply(PatternFunction f) {
    return f.caseNotAllowed(this);
  }
}
