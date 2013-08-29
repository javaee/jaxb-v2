package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Parseable;
import org.xml.sax.Locator;

/**
 * Base class of all the patterns.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class DPattern implements ParsedPattern {
    Locator location;
    DAnnotation annotation;

    /**
     * Used to chain the child patterns in a doubly-linked list.
     */
    DPattern next;
    DPattern prev;

    /**
     * Returns where the pattern is defined in the source code.
     */
    public Locator getLocation() {
        return location;
    }

    /**
     * Returns the annotation associated with it.
     *
     * @return
     *      may be empty, but never be null.
     */
    public DAnnotation getAnnotation() {
        if(annotation==null)
            return DAnnotation.EMPTY;
        return annotation;
    }

    /**
     * Returns true if this pattern is nullable.
     *
     * A nullable pattern is a pattern that can match the empty sequence.
     */
    public abstract boolean isNullable();

    public abstract <V> V accept( DPatternVisitor<V> visitor );

    /**
     * Creates a {@link Parseable} object that reparses this pattern.
     */
    public Parseable createParseable() {
        return new PatternParseable(this);
    }

    /**
     * Returns true if this is {@link DElementPattern}.
     */
    public final boolean isElement() {
        return this instanceof DElementPattern;
    }

    /**
     * Returns true if this is {@link DAttributePattern}.
     */
    public final boolean isAttribute() {
        return this instanceof DAttributePattern;
    }
}
