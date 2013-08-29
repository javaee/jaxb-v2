package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.nc.ChoiceNameClass;
import org.kohsuke.rngom.nc.NameClass;

class Alphabet {
  private NameClass nameClass;

  boolean isEmpty() {
    return nameClass == null;
  }

  void addElement(NameClass nc) {
    if (nameClass == null)
      nameClass = nc;
    else if (nc != null)
      nameClass = new ChoiceNameClass(nameClass, nc);
  }

  void addAlphabet(Alphabet a) {
    addElement(a.nameClass);
  }

  void checkOverlap(Alphabet a) throws RestrictionViolationException {
    if (nameClass != null
	&& a.nameClass != null
	&& nameClass.hasOverlapWith(a.nameClass))
      throw new RestrictionViolationException("interleave_element_overlap");
  }
}
