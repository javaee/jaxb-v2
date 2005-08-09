package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.Name;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Represents an XML tag name (and attributes for start tags.)
 *
 * <p>
 * This object is used so reduce the number of method call parameters
 * among unmarshallers.
 *
 * An instance of this is expected to be reused by the caller of
 * {@link XmlVisitor}. Note that the rest of the unmarshaller may
 * modify any of the fields while processing an event (such as to
 * intern strings, replace attributes),
 * so {@link XmlVisitor} should reset all fields for each use.
 *
 * <p>
 * The 'qname' parameter, which holds the qualified name of the tag
 * (such as 'foo:bar' or 'zot'), is not used in the typical unmarshalling
 * route and it's also expensive to compute for some input.
 * Thus this parameter is computed lazily.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TagName {
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
     * Used only for the enterElement event.
     * Otherwise the value is undefined.
     *
     * This might be {@link AttributesEx}.
     */
    public Attributes atts;

    public TagName() {
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
    public final boolean matches( Name name ) {
        return this.local==name.localName && this.uri==name.nsUri;
    }

//    /**
//     * @return
//     *      Can be empty but always non-null. NOT interned.
//     */
//    public final String getPrefix() {
//        int idx = qname.indexOf(':');
//        if(idx<0)   return "";
//        else        return qname.substring(0,idx);
//    }

    public String toString() {
        return '{'+uri+'}'+local;
    }

    /**
     * Removes the specified attribute.
     *
     * This isn't used frequently, so it doesn't need to be fast.
     */
    // TODO: not sure if it should be here
    public void eatAttribute(int idx) {
        AttributesImpl a = new AttributesImpl(atts);
        a.removeAttribute(idx);
        atts = a;
    }

    /**
     * Gets the qualified name of the tag.
     *
     * @return never null.
     */
    public abstract String getQname();
}
