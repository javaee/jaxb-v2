package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link Locatable} implementation for a field.
 *
 * @author Kohsuke Kawaguchi
 */
public class FieldLocatable<FieldT> implements Locatable {
    private final Locatable upstream;
    private final FieldT field;
    private final Navigator<?,?,FieldT,?> nav;

    public FieldLocatable(Locatable upstream, FieldT field, Navigator<?,?,FieldT,?> nav) {
        this.upstream = upstream;
        this.field = field;
        this.nav = nav;
    }

    public Locatable getUpstream() {
        return upstream;
    }

    public Location getLocation() {
        return nav.getFieldLocation(field);
    }
}
