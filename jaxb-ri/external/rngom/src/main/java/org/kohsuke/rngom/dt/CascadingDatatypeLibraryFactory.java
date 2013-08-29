package org.kohsuke.rngom.dt;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

/**
 * 
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class CascadingDatatypeLibraryFactory implements DatatypeLibraryFactory {
    private final DatatypeLibraryFactory factory1;
    private final DatatypeLibraryFactory factory2;

    public CascadingDatatypeLibraryFactory( DatatypeLibraryFactory factory1, DatatypeLibraryFactory factory2) {
        this.factory1 = factory1;
        this.factory2 = factory2;
    }
    
    public DatatypeLibrary createDatatypeLibrary(String namespaceURI) {
        DatatypeLibrary lib = factory1.createDatatypeLibrary(namespaceURI);
        if(lib==null)
            lib = factory2.createDatatypeLibrary(namespaceURI);
        return lib;
    }

}
