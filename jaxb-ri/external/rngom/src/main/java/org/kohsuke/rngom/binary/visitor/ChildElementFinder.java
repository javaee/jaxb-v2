package org.kohsuke.rngom.binary.visitor;

import org.kohsuke.rngom.binary.Pattern;
import org.kohsuke.rngom.nc.NameClass;

import java.util.HashSet;
import java.util.Set;

/**
 * Visits a pattern and creates a list of possible child elements.
 *
 * <p>
 * One can use a similar technique to introspect a pattern.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class ChildElementFinder extends PatternWalker {

    private final Set children = new HashSet();

    /**
     * Represents a child element.
     */
    public static class Element {
        public final NameClass nc;
        public final Pattern content;

        public Element(NameClass nc, Pattern content) {
            this.nc = nc;
            this.content = content;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Element)) return false;

            final Element element = (Element) o;

            if (content != null ? !content.equals(element.content) : element.content != null) return false;
            if (nc != null ? !nc.equals(element.nc) : element.nc != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (nc != null ? nc.hashCode() : 0);
            result = 29 * result + (content != null ? content.hashCode() : 0);
            return result;
        }
    }

    /**
     * Returns a set of {@link Element}.
     */
    public Set getChildren() {
        return children;
    }

    public void visitElement(NameClass nc, Pattern content) {
        children.add(new Element(nc,content));
    }

    public void visitAttribute(NameClass ns, Pattern value) {
        // there will be no element inside attribute,
        // so don't go in there.
    }

    public void visitList(Pattern p) {
        // there will be no element inside a list,
        // so don't go in there.
    }
}
