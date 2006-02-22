package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JExpr;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.xml.bind.v2.ContextFactory;

/**
 * @author Kohsuke Kawaguchi
 */
final class DualObjectFactoryGenerator extends ObjectFactoryGenerator {
    private final ObjectFactoryGenerator publicOFG;
    private final ObjectFactoryGenerator privateOFG;

    DualObjectFactoryGenerator(BeanGenerator outline, Model model, JPackage targetPackage) {
        this.publicOFG = new PublicObjectFactoryGenerator(outline,model,targetPackage);
        this.privateOFG = new PrivateObjectFactoryGenerator(outline,model,targetPackage);

        // put the marker so that we can detect missing jaxb.properties
        publicOFG.getObjectFactory().field(JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
                Void.class, ContextFactory.USE_JAXB_PROPERTIES, JExpr._null());
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
