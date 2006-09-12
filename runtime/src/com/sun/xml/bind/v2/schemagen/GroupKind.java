package com.sun.xml.bind.v2.schemagen;

import com.sun.xml.bind.v2.schemagen.xmlschema.Particle;
import com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer;

/**
 * Enum for model group type.
 *
 * @author Kohsuke Kawaguchi
 */
enum GroupKind {
    ALL("all"), SEQUENCE("sequence"), CHOICE("choice");

    private final String name;

    GroupKind(String name) {
        this.name = name;
    }

    /**
     * Writes the model group.
     */
    Particle write(ContentModelContainer parent) {
        return parent._element(name,Particle.class);
    }
}
