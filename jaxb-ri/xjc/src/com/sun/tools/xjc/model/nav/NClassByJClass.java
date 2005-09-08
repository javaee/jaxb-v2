package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

/**
 * @author Kohsuke Kawaguchi
 */
class NClassByJClass implements NClass {
    /*package*/ final JClass clazz;

    NClassByJClass(JClass clazz) {
        this.clazz = clazz;
    }

    public JClass toType(Outline o, Aspect aspect) {
        return clazz;
    }

    public boolean isAbstract() {
        return clazz.isAbstract();
    }

    public boolean isBoxedType() {
        return clazz.getPrimitiveType()!=null;
    }

    public String fullName() {
        return clazz.fullName();
    }
}
