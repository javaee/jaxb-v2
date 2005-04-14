package com.sun.tools.txw2.model;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.txw2.model.prop.Prop;
import org.xml.sax.Locator;

import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class Empty extends Leaf {
    public Empty(Locator location) {
        super(location);
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        // noop
    }
}
