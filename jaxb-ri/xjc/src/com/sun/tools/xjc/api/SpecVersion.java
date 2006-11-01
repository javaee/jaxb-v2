package com.sun.tools.xjc.api;

/**
 * Represents the spec version constant. 
 *
 * @author Kohsuke Kawaguchi
 */
public enum SpecVersion {
    V2_0, V2_1;

    /**
     * Returns true if this version is equal or later than the given one.
     */
    public boolean isLaterThan(SpecVersion t) {
        return this.ordinal()>=t.ordinal();
    }

    /**
     * Parses "2.0" and "2.1" into the {@link SpecVersion} object.
     *
     * @return null for parsing failure.
     */
    public static SpecVersion parse(String token) {
        if(token.equals("2.0"))
            return V2_0;
        else
        if(token.equals("2.1"))
            return V2_1;
        return null;
    }

    public static final SpecVersion LATEST = V2_1;
}
