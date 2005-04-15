package com.sun.xml.bind.v2.model.core;

/**
 * Mode of the wildcard.
 *
 * @author Kohsuke Kawaguchi
 */
public enum WildcardMode {
    STRICT(false,true), SKIP(true,false), LAX(true,true);

    public final boolean allowDom;
    public final boolean allowTypedObject;

    WildcardMode(boolean allowDom, boolean allowTypedObject) {
        this.allowDom = allowDom;
        this.allowTypedObject = allowTypedObject;
    }
}
