package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;

/**
 * @author Kohsuke Kawaguchi
 */
final class DualObjectFactoryGenerator extends ObjectFactoryGenerator {
    private final ObjectFactoryGenerator publicOFG;
    private final ObjectFactoryGenerator privateOFG;

    DualObjectFactoryGenerator(BeanGenerator outline, Model model, JPackage targetPackage) {
        this.publicOFG = new PublicObjectFactoryGenerator(outline,model,targetPackage);
        this.privateOFG = new PrivateObjectFactoryGenerator(outline,model,targetPackage);
    }

    void populate(CElementInfo ei) {
        publicOFG.populate(ei);
        privateOFG.populate(ei);
    }

    void populate(ClassOutlineImpl cc) {
        publicOFG.populate(cc);
        privateOFG.populate(cc);
    }

    public JDefinedClass getObjectFactory() {
        return publicOFG.getObjectFactory();
    }
}
