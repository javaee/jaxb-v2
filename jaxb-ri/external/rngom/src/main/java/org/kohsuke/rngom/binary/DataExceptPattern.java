package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;
import org.relaxng.datatype.Datatype;
import org.xml.sax.Locator;

public class DataExceptPattern extends DataPattern {
  private Pattern except;
  private Locator loc;

  DataExceptPattern(Datatype dt, Pattern except, Locator loc) {
    super(dt);
    this.except = except;
    this.loc = loc;
  }

  boolean samePattern(Pattern other) {
    if (!super.samePattern(other))
      return false;
    return except.samePattern(((DataExceptPattern)other).except);
  }

  public void accept(PatternVisitor visitor) {
    visitor.visitDataExcept(getDatatype(), except);
  }

  public Object apply(PatternFunction f) {
    return f.caseDataExcept(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    super.checkRestrictions(context, dad, alpha);
    try {
      except.checkRestrictions(DATA_EXCEPT_CONTEXT, null, null);
    }
    catch (RestrictionViolationException e) {
      e.maybeSetLocator(loc);
      throw e;
    }
  }

  Pattern getExcept() {
    return except;
  }
}
