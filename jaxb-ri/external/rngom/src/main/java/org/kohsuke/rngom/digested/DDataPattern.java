package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.parse.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DDataPattern extends DPattern {
    DPattern except;

    String datatypeLibrary;
    String type;

    final List<Param> params = new ArrayList<Param>();

    /**
     * Parameter to a data pattern.
     */
    public final class Param {
        String name;
        String value;
        Context context;
        String ns;
        Location loc;
        Annotation anno;

        public Param(String name, String value, Context context, String ns, Location loc, Annotation anno) {
            this.name = name;
            this.value = value;
            this.context = context;
            this.ns = ns;
            this.loc = loc;
            this.anno = anno;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public Context getContext() {
            return context;
        }

        public String getNs() {
            return ns;
        }

        public Location getLoc() {
            return loc;
        }

        public Annotation getAnno() {
            return anno;
        }
    }

    /**
     * Gets the datatype library URI.
     *
     * @return
     *      Can be empty (which represents the built-in datatypes), but never null.
     */
    public String getDatatypeLibrary() {
        return datatypeLibrary;
    }

    /**
     * Gets the datatype name, such as "int" or "token".
     *
     * @return
     *      never null.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the parameters of this &lt;data pattern.
     *
     * @return
     *      can be empty but never null.
     */
    public List<Param> getParams() {
        return params;
    }

    /**
     * Gets the pattern that reprsents the &lt;except> child of this data pattern.
     *
     * @return null if not exist.
     */
    public DPattern getExcept() {
        return except;
    }

    public boolean isNullable() {
        return false;
    }

    public Object accept( DPatternVisitor visitor ) {
        return visitor.onData(this);
    }
}
