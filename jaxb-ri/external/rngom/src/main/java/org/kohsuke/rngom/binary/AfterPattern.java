package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;


public class AfterPattern extends BinaryPattern {
  AfterPattern(Pattern p1, Pattern p2) {
    super(false,
	  combineHashCode(AFTER_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }

  boolean isNotAllowed() {
    return p1.isNotAllowed();
  }

  public Object apply(PatternFunction f) {
    return f.caseAfter(this);
  }
  public void accept(PatternVisitor visitor) {
      visitor.visitAfter(p1,p2);
  }
}
