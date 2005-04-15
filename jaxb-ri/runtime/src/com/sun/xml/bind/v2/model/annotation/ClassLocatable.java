package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link Locatable} implementation for a class.
 * 
 * @author Kohsuke Kawaguchi
 */
public class ClassLocatable<ClassDeclT> implements Locatable {
    private final Locatable upstream;
    private final ClassDeclT clazz;
    private final Navigator<?,ClassDeclT,?,?> nav;

    public ClassLocatable(Locatable upstream, ClassDeclT clazz, Navigator<?,ClassDeclT,?,?> nav) {
        this.upstream = upstream;
        this.clazz = clazz;
        this.nav = nav;
    }

    public Locatable getUpstream() {
        return upstream;
    }

    public Location getLocation() {
        return nav.getClassLocation(clazz);
    }
}
