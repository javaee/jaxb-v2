package com.sun.xml.bind.v2.schemagen;

import java.util.ArrayList;
import java.util.List;

import com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer;
import com.sun.xml.bind.v2.schemagen.xmlschema.Particle;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle;

/**
 * Abstration for writing out content models.
 *
 * <p>
 * This mechanism is used to avoid generating redundant model groups.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ContentModelWriter {
    /**
     * Returns false if the {@link #write(ContentModelContainer)} method
     * needs to write a particle, not a model group.
     *
     * IOW, occurence, element declaration, or wildcard.
     */
    protected boolean canBeTopLevel() { return false; }

    /**
     * Writes out the content model.
     *
     * Normall this runs recursively until we write out the whole content model.
     */
    protected abstract void write(ContentModelContainer parent);

    /**
     * Intermediate node that writes out a model gorup or a particle of a model group.
     */
    static class ModelGroup extends ContentModelWriter {
        /**
         * {@link ContentModelWriter} that writes particles inside this content model.
         */
        private final List<ContentModelWriter> children = new ArrayList<ContentModelWriter>();
        private final GroupKind kind;
        private final boolean repeated;
        private final boolean optional;

        public ModelGroup(GroupKind kind, boolean optional, boolean repeated) {
            this.kind = kind;
            this.repeated = repeated;
            this.optional = optional;
        }

        public boolean canBeTopLevel() {
            if(repeated || optional)    return false;
            if(children.size()==1)
                // if there's only one child, we might unwrap.
                return children.get(0).canBeTopLevel();
            else
                return true;
        }

        public void add(ContentModelWriter child) {
            children.add(child);
        }

        public void write(ContentModelContainer parent) {
            if(repeated || optional || children.size()!=1) {
                // need this model group
                Particle c = kind.write(parent);
                if(repeated)
                    c.maxOccurs("unbounded");
                if(optional)
                    c.minOccurs(0);

                parent = c;
            }

            for (ContentModelWriter child : children) {
                child.write(parent);
            }
        }

        /**
         * Writes inside the given complex type.
         */
        public void write(TypeDefParticle ct) {
            write(ct._cast(ContentModelContainer.class));
        }
    }
}
