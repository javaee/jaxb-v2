package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.impl.UName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class Step<T extends XSComponent> {
    public final Axis<? extends T> axis;

    /**
     * 'Predicate' in SCD designates the index of the item. -1 if there's no predicate.
     * TODO: check if it's 0-origin or 1-origin.
     *
     * <p>
     * Because of the parsing order this parameter cannot be marked
     * final, even though it's immutable once it's parsed.
     */
    int predicate = -1;

    protected Step(Axis<? extends T> axis) {
        this.axis = axis;
    }

    /**
     * Evaluate this step against the current node set
     * and returns matched nodes.
     */
    public final List<T> evaluate(Context context) {
        List<T> result = new ArrayList<T>();

        for( XSComponent contextNode : context.nodeSet ) {
            for( T node : axis.iterator(contextNode) ) {
                if(match(node) && !result.contains(node))
                    result.add(node);
            }
        }

        if(predicate>=0) {
            if(predicate>=result.size())
                result = Collections.EMPTY_LIST;
            else
                result = Collections.singletonList(result.get(predicate));
        }

        return result;
    }

    /**
     * Returns true if the node matches this step.
     */
    protected abstract boolean match(T node);


    /**
     * Matches any name.
     */
    static final class Any extends Step<XSComponent> {
        public Any(Axis<? extends XSComponent> axis) {
            super(axis);
        }

        protected boolean match(XSComponent node) {
            return true;
        }
    }

    /**
     * Matches a particular name.
     */
    static final class Named extends Step<XSDeclaration> {
        private final String nsUri;
        private final String localName;

        public Named(Axis<? extends XSDeclaration> axis, UName n) {
            this(axis,n.getNamespaceURI(),n.getName());
        }

        public Named(Axis<? extends XSDeclaration> axis, String nsUri, String localName) {
            super(axis);
            this.nsUri = nsUri;
            this.localName = localName;
        }

        protected boolean match(XSDeclaration d) {
            return d.getName().equals(localName) && d.getTargetNamespace().equals(nsUri);
        }
    }

    /**
     * Matches anonymous types.
     */
    static final class AnonymousType extends Step<XSType> {
        public AnonymousType(Axis<? extends XSType> axis) {
            super(axis);
        }

        protected boolean match(XSType node) {
            return node.isLocal();
        }
    }

    /**
     * Matches a particular kind of facets.
     */
    static final class Facet extends Step<XSFacet> {
        private final String name;
        public Facet(Axis<? extends XSFacet> axis, String facetName) {
            super(axis);
            this.name = facetName;
        }

        protected boolean match(XSFacet f) {
            return f.getName().equals(name);
        }
    }
}
