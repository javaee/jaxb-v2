package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class TextPattern extends Pattern {
  TextPattern() {
    super(true, MIXED_CONTENT_TYPE, TEXT_HASH_CODE);
  }

  boolean samePattern(Pattern other) {
    return other instanceof TextPattern;
  }

  public void accept(PatternVisitor visitor) {
    visitor.visitText();
  }

  public Object apply(PatternFunction f) {
    return f.caseText(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_text");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_text");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_text");
    }
  }

}
