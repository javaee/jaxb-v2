package com.sun.xml.bind.v2.runtime;

/**
 * Namespace URIs and local names sorted by their indices.
 * Number of Names used for EIIs and AIIs
 *
 * @author Kohsuke Kawaguchi
 */
public final class NameList {
    /**
     * Namespace URIs by their indices. No nulls in this array.
     * Read-only.
     */
    public final String[] namespaceURIs;

    /**
     * Local names by their indices. No nulls in this array.
     * Read-only.
     */ 
    public final String[] localNames;

    /**
     * Number of Names for elements
     */
    public final int numberOfElementNames;
    
    /**
     * Number of Names for attributes
     */
    public final int numberOfAttributeNames;
    
    public NameList(String[] namespaceURIs, String[] localNames, int numberElementNames, int numberAttributeNames) {
        this.namespaceURIs = namespaceURIs;
        this.localNames = localNames;
        this.numberOfElementNames = numberElementNames;
        this.numberOfAttributeNames = numberAttributeNames;
    }
}
