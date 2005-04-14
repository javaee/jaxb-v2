package com.sun.tools.txw2.model;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.tools.txw2.model.prop.Prop;
import org.xml.sax.Locator;

import java.util.Set;

/**
 * List of {@link Data} or {@link Value}.
 *
 * @author Kohsuke Kawaguchi
 */
public class List extends Node implements Text {
    public List(Locator location, Leaf leaf) {
        super(location, leaf);
    }

    public JType getDatatype(NodeSet nset) {
        if(hasOneChild() && leaf instanceof Text) {
            return ((Text)leaf).getDatatype(nset).array();
        } else {
            return nset.codeModel.ref(String.class).array();
        }
    }

    void generate(JDefinedClass clazz, NodeSet nset, Set<Prop> props) {
        createDataMethod(clazz,getDatatype(nset),nset,props);
    }
}
