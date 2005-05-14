package com.sun.tools.txw2.model;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.txw2.model.prop.Prop;
import org.xml.sax.Locator;

import java.util.Set;

/**
 * A reference to a named pattern.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Ref extends Leaf {
    public final Define def;

    public Ref(Locator location, Grammar scope, String name) {
        super(location);
        this.def = scope.get(name);
    }

    public Ref(Locator location, Define def) {
        super(location);
        this.def = def;
    }

    public boolean isInline() {
        return def.isInline();
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        def.generate(clazz,nset,props);
    }
}
