package com.sun.xml.bind.v2.runtime.unmarshaller;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.runtime.Name;

import org.xml.sax.Attributes;

/**
 * Parameter of the {enter|leave}{element|attribute} event.
 *
 * <p>
 * For {@link UnmarshallingEventHandler}s, this object should be
 * considered as immutable. But it is made mutable so that
 * {@link UnmarshallingContext} can reuse them.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class EventArg {
    /**
     * URI of the attribute/element name.
     *
     * Can be empty, but never null. Interned.
     */
    public String uri;
    /**
     * Local part of the attribute/element name.
     *
     * Never be null. Interned.
     */
    public String local;

    /**
     * Rawname of the attribute/element name (e.g., "foo:bar")
     *
     * Never be null. Interned.
     */
    public String qname;

    /**
     * Used only for the enterElement event.
     * Otherwise the value is undefined.
     *
     * This might be {@link AttributesEx}.
     */
    public Attributes atts;

    public EventArg() {
    }

    public EventArg(String uri, String local, String qname, Attributes atts) {
        this.uri = uri;
        this.local = local;
        this.qname = qname;
        this.atts = atts;
    }

    /**
     * Creates a {@link QName} object from the name triplet.
     */
    public final QName createQName() {
        return new QName(uri,local,getPrefix());
    }

    /**
     * Checks if the given name pair matches this name.
     */
    public final boolean matches( String nsUri, String local ) {
        return this.uri==nsUri && this.local==local;
    }

    /**
     * Checks if the given name pair matches this name.
     */
    public final boolean matches( QName name ) {
        return this.local==name.getLocalPart() && this.uri==name.getNamespaceURI();
    }

    public final boolean matches( Name name ) {
        return this.local==name.localName && this.uri==name.nsUri;
    }

    /**
     * @return
     *      Can be empty but always non-null. NOT interned.
     */
    public final String getPrefix() {
        int idx = qname.indexOf(':');
        if(idx<0)   return "";
        else        return qname.substring(0,idx);
    }

    public String toString() {
        return '{'+uri+'}'+local;
    }
}
