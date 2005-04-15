package com.sun.xml.bind.v2.runtime;

/**
 * Namespace URIs and local names sorted by their indices.
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

    public NameList(String[] namespaceURIs, String[] localNames) {
        this.namespaceURIs = namespaceURIs;
        this.localNames = localNames;
    }
}
