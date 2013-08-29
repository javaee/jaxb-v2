package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.parse.Context;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DValuePattern extends DPattern {
    private String datatypeLibrary;
    private String type;
    private String value;
    private Context context;
    private String ns;

    public DValuePattern(String datatypeLibrary, String type, String value, Context context, String ns) {
        this.datatypeLibrary = datatypeLibrary;
        this.type = type;
        this.value = value;
        this.context = context;
        this.ns = ns;
    }

    public String getDatatypeLibrary() {
        return datatypeLibrary;
    }

    public String getType() {
        return type;
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

    public boolean isNullable() {
        return false;
    }

    public Object accept( DPatternVisitor visitor ) {
        return visitor.onValue(this);
    }
}
