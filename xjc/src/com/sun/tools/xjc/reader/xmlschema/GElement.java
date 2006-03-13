package com.sun.tools.xjc.reader.xmlschema;

import java.util.HashSet;
import java.util.Set;

import com.sun.tools.xjc.reader.gbind.Element;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class GElement extends Element {
    /**
     * All the {@link XSParticle}s (whose term is {@link XSElementDecl})
     * that are coereced into a single {@link Element}.
     */
    final Set<XSParticle> particles = new HashSet<XSParticle>();

    /**
     * Gets the seed (raw XML name) to be used to generate a property name.
     */
    abstract String getPropertyNameSeed();
}
