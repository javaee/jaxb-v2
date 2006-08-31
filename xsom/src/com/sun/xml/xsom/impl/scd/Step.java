package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.impl.UName;

import java.util.Iterator;

/**
 * Building block of {@link SCD}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Step<T extends XSComponent> {
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
    public final Iterator<T> evaluate(Context context) {
        // list up the whole thing
        Iterator<T> r = new Iterators.Map<T,XSComponent>(context.nodeSet) {
            protected Iterator<T> apply(XSComponent contextNode) {
                return new Iterators.Filter<T>(axis.iterator(contextNode)) {
                    protected boolean matches(T value) {
                        return match(value);
                    }
                };
            }
        };

        // avoid duplicates
        r = new Iterators.Unique<T>(r);

        if(predicate>=0) {
            T item=null;
            for( int i=predicate; i>=0; i-- ) {
                if(!r.hasNext())
                    return Iterators.empty();
                item = r.next();
            }
            return new Iterators.Singleton<T>(item);
        }

        return r;
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
