package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractAxisImpl<T extends XSComponent> implements Axis<T>, XSFunction<Iterator<T>> {
    /**
     * Creates a singleton list.
     */
    protected final Iterator<T> singleton(T t) {
        return new Iterators.Singleton<T>(t);
    }

    public Iterator<T> iterator(XSComponent contextNode) {
        return contextNode.apply(this);
    }

    public Iterator<T> annotation(XSAnnotation ann) {
        return empty();
    }

    public Iterator<T> attGroupDecl(XSAttGroupDecl decl) {
        return empty();
    }

    public Iterator<T> attributeDecl(XSAttributeDecl decl) {
        return empty();
    }

    public Iterator<T> attributeUse(XSAttributeUse use) {
        return empty();
    }

    public Iterator<T> complexType(XSComplexType type) {
        return empty();
    }

    public Iterator<T> schema(XSSchema schema) {
        return empty();
    }

    public Iterator<T> facet(XSFacet facet) {
        return empty();
    }

    public Iterator<T> notation(XSNotation notation) {
        return empty();
    }

    public Iterator<T> identityConstraint(XSIdentityConstraint decl) {
        return empty();
    }

    public Iterator<T> xpath(XSXPath xpath) {
        return empty();
    }

    public Iterator<T> simpleType(XSSimpleType simpleType) {
        return empty();
    }

    public Iterator<T> particle(XSParticle particle) {
        return empty();
    }

    public Iterator<T> empty(XSContentType empty) {
        return empty();
    }

    public Iterator<T> wildcard(XSWildcard wc) {
        return empty();
    }

    public Iterator<T> modelGroupDecl(XSModelGroupDecl decl) {
        return empty();
    }

    public Iterator<T> modelGroup(XSModelGroup group) {
        return empty();
    }

    public Iterator<T> elementDecl(XSElementDecl decl) {
        return empty();
    }

    /**
     * Returns an empty list.
     */
    protected final Iterator<T> empty() {
        // we need to run on JDK 1.4
        return Collections.EMPTY_LIST.iterator();
    }

}
