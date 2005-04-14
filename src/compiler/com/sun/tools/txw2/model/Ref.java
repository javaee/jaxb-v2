package com.sun.tools.txw2.model;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.txw2.model.prop.Prop;
import org.xml.sax.Locator;

import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public final class Ref extends Leaf {
    public final Define def;

    public Ref(Locator location, Grammar scope, String name) {
        super(location);
        this.def = scope.get(name);
    }

    public boolean isInline() {
        return def.isInline();
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        def.generate(clazz,nset,props);
    }
}
