package org.kohsuke.rngom.dt;

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

/**
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class CachedDatatypeLibraryFactory implements DatatypeLibraryFactory {
    
    private String lastUri;
    private DatatypeLibrary lastLib;
    
    private final DatatypeLibraryFactory core;
    
    public CachedDatatypeLibraryFactory( DatatypeLibraryFactory core ) {
        this.core = core;
    }
    
    public DatatypeLibrary createDatatypeLibrary(String namespaceURI) {
        if( lastUri==namespaceURI )
            return lastLib;
        
        lastUri = namespaceURI;
        lastLib = core.createDatatypeLibrary(namespaceURI);
        return lastLib;
    }

}
