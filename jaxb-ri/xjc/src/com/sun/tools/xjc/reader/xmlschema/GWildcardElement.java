package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.gbind.Element;

/**
 * {@link Element} that represents a wildcard,
 * for the "ease of binding" we always just bind this to DOM elements.
 * @author Kohsuke Kawaguchi
 */
final class GWildcardElement extends GElement {
    public String toString() {
        return "#any";
    }

    String getPropertyNameSeed() {
        return "any";
    }
}
