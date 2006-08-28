package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.model.CElement;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;

/**
 * {@link ClassBinder} that marks abstract components as abstract.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class Abstractifier extends ClassBinderFilter {
    public Abstractifier(ClassBinder core) {
        super(core);
    }

    public CElement complexType(XSComplexType xs) {
        CElement ci = super.complexType(xs);
        if(ci!=null && xs.isAbstract())
            ci.setAbstract();
        return ci;
    }

    public CElement elementDecl(XSElementDecl xs) {
        CElement ci = super.elementDecl(xs);
        if(ci!=null && xs.isAbstract())
            ci.setAbstract();
        return ci;
    }
}
