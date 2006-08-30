package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
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
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.visitor.XSFunction;

import java.util.Collections;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractAxisImpl<T extends XSComponent> implements Axis<T>, XSFunction<List<T>> {

    /**
     * Creates a singleton list.
     */
    protected final List<T> singleton(T t) {
        if(t==null)     return empty();
        else            return Collections.singletonList(t);
    }

    public List<T> iterator(XSComponent contextNode) {
        return contextNode.apply(this);
    }

    public List<T> annotation(XSAnnotation ann) {
        return empty();
    }

    public List<T> attGroupDecl(XSAttGroupDecl decl) {
        return empty();
    }

    public List<T> attributeDecl(XSAttributeDecl decl) {
        return empty();
    }

    public List<T> attributeUse(XSAttributeUse use) {
        return empty();
    }

    public List<T> complexType(XSComplexType type) {
        return empty();
    }

    public List<T> schema(XSSchema schema) {
        return empty();
    }

    public List<T> facet(XSFacet facet) {
        return empty();
    }

    public List<T> notation(XSNotation notation) {
        return empty();
    }

    public List<T> identityConstraint(XSIdentityConstraint decl) {
        return empty();
    }

    public List<T> xpath(XSXPath xpath) {
        return empty();
    }

    public List<T> simpleType(XSSimpleType simpleType) {
        return empty();
    }

    public List<T> particle(XSParticle particle) {
        return empty();
    }

    public List<T> empty(XSContentType empty) {
        return empty();
    }

    public List<T> wildcard(XSWildcard wc) {
        return empty();
    }

    public List<T> modelGroupDecl(XSModelGroupDecl decl) {
        return empty();
    }

    public List<T> modelGroup(XSModelGroup group) {
        return empty();
    }

    public List<T> elementDecl(XSElementDecl decl) {
        return empty();
    }

    /**
     * Returns an empty list.
     */
    protected final List<T> empty() {
        // we need to run on JDK 1.4
        return Collections.EMPTY_LIST;
    }

}
