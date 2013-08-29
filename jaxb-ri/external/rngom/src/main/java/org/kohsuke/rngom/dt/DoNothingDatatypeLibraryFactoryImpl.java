package org.kohsuke.rngom.dt;

import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.helpers.StreamingValidatorImpl;

/**
 * {@link DatatypeLibraryFactory} implementation
 * that returns a dummy {@link Datatype}.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class DoNothingDatatypeLibraryFactoryImpl implements DatatypeLibraryFactory {
    public DatatypeLibrary createDatatypeLibrary(String s) {
        return new DatatypeLibrary() {

            public Datatype createDatatype(String s) throws DatatypeException {
                return createDatatypeBuilder(s).createDatatype();
            }

            public DatatypeBuilder createDatatypeBuilder(String s) throws DatatypeException {
                return new DatatypeBuilder() {
                    public void addParameter(String s, String s1, ValidationContext validationContext) throws DatatypeException {
                    }

                    public Datatype createDatatype() throws DatatypeException {
                        return new Datatype() {

                            public boolean isValid(String s, ValidationContext validationContext) {
                                return false;
                            }

                            public void checkValid(String s, ValidationContext validationContext) throws DatatypeException {
                            }

                            public DatatypeStreamingValidator createStreamingValidator(ValidationContext validationContext) {
                                return new StreamingValidatorImpl(this,validationContext);
                            }

                            public Object createValue(String s, ValidationContext validationContext) {
                                return null;
                            }

                            public boolean sameValue(Object o, Object o1) {
                                return false;
                            }

                            public int valueHashCode(Object o) {
                                return 0;
                            }

                            public int getIdType() {
                                return ID_TYPE_NULL;
                            }

                            public boolean isContextDependent() {
                                return false;
                            }
                        };
                    }
                };
            }
        };
    }
}
