package org.kohsuke.rngom.dt.builtin;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

import org.kohsuke.rngom.util.Localizer;

class BuiltinDatatypeBuilder implements DatatypeBuilder {
  private final Datatype dt;
  
  private static final Localizer localizer = new Localizer(BuiltinDatatypeBuilder.class);
  
  BuiltinDatatypeBuilder(Datatype dt) {
    this.dt = dt;
  }

  public void addParameter(String name,
			   String value,
			   ValidationContext context) throws DatatypeException {
    throw new DatatypeException(localizer.message("builtin_param"));
  }

  public Datatype createDatatype() {
    return dt;
  }
}
