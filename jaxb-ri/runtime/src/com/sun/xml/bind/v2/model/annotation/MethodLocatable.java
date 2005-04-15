package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link Locatable} implementation for a method.
 * 
 * @author Kohsuke Kawaguchi
 */
public class MethodLocatable<MethodT> implements Locatable {
    private final Locatable upstream;
    private final MethodT method;
    private final Navigator<?,?,?,MethodT> nav;

    public MethodLocatable(Locatable upstream, MethodT method, Navigator<?,?,?,MethodT> nav) {
        this.upstream = upstream;
        this.method = method;
        this.nav = nav;
    }

    public Locatable getUpstream() {
        return upstream;
    }

    public Location getLocation() {
        return nav.getMethodLocation(method);
    }
}
