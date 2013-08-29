package org.kohsuke.rngom.binary;

abstract class StringPattern extends Pattern {
  StringPattern(int hc) {
    super(false, DATA_CONTENT_TYPE, hc);
  }
}
