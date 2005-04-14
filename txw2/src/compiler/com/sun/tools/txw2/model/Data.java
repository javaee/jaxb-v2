package com.sun.tools.txw2.model;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.tools.txw2.model.prop.Prop;
import org.xml.sax.Locator;

import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class Data extends Leaf implements Text {
    /**
     * The Java representation of the datatype.
     */
    public final JType type;

    public Data(Locator location, JType type) {
        super(location);
        this.type = type;
    }

    public JType getDatatype(NodeSet nset) {
        return type;
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        createDataMethod(clazz,type,nset,props);
    }
}
