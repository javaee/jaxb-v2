package org.kohsuke.rngom.dt.builtin;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

import org.kohsuke.rngom.xml.util.WellKnownNamespaces;

/**
 * {@link DatatypeLibraryFactory} for
 * RELAX NG Built-in datatype library and compatibility datatype library.
 */
public class BuiltinDatatypeLibraryFactory implements DatatypeLibraryFactory {
    private final DatatypeLibrary builtinDatatypeLibrary;
    private final DatatypeLibrary compatibilityDatatypeLibrary;
    /**
     * Target of delegation.
     */
    private final DatatypeLibraryFactory core;

    public BuiltinDatatypeLibraryFactory( DatatypeLibraryFactory coreFactory ) {
        builtinDatatypeLibrary = new BuiltinDatatypeLibrary(coreFactory);
        compatibilityDatatypeLibrary = new CompatibilityDatatypeLibrary(coreFactory);
        this.core = coreFactory;
    }
    
    public DatatypeLibrary createDatatypeLibrary(String uri) {
        if (uri.equals(""))
            return builtinDatatypeLibrary;
        if (uri.equals(WellKnownNamespaces.RELAX_NG_COMPATIBILITY_DATATYPES))
            return compatibilityDatatypeLibrary;
        return core.createDatatypeLibrary(uri);
    }
}
