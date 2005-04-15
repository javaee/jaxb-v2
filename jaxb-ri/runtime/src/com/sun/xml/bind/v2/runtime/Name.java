package com.sun.xml.bind.v2.runtime;

import javax.xml.namespace.QName;

/**
 * The internal representation of an XML name.
 *
 * <p>
 * This class keeps indicies for URI and local name for enabling faster processing.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Name {
    /**
     * Namespace URI. interned.
     */
    public final String nsUri;

    /**
     * Local name. interned.
     */
    public final String localName;

    /**
     * Index -1 is reserved for representing the empty namespace URI of attributes.
     */
    public final short nsUriIndex;
    public final short localNameIndex;

    Name(int nsIndex, String nsUri, int localIndex, String localName) {
        this.nsUri = nsUri;
        this.localName = localName;
        this.nsUriIndex = (short)nsIndex;
        this.localNameIndex = (short)localIndex;
    }

    public String toString() {
        return '{'+nsUri+'}'+localName;
    }

    /**
     * Creates a {@link QName} from this.
     */
    public QName toQName() {
        return new QName(nsUri,localName);
    }

    public boolean equals( String nsUri, String localName ) {
        return localName.equals(this.localName) && nsUri.equals(this.nsUri);
    }
}
