package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;
import org.relaxng.datatype.Datatype;

public class DataPattern extends StringPattern {
  private Datatype dt;

  DataPattern(Datatype dt) {
    super(combineHashCode(DATA_HASH_CODE, dt.hashCode()));
    this.dt = dt;
  }

  boolean samePattern(Pattern other) {
    if (other.getClass() != this.getClass())
      return false;
    return dt.equals(((DataPattern)other).dt);
  }

  public void accept(PatternVisitor visitor) {
    visitor.visitData(dt);
  }

  public Object apply(PatternFunction f) {
    return f.caseData(this);
  }

  Datatype getDatatype() {
    return dt;
  }

  boolean allowsAnyString() {
      return false;
//    return dt instanceof Datatype2 && ((Datatype2)dt).alwaysValid();
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_data");
    }
  }
}
