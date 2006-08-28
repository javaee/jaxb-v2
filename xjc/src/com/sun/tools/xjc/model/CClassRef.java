package com.sun.tools.xjc.model;

import javax.xml.namespace.QName;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.xml.xsom.XSComponent;

/**
 * Refernece to an existing class.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CClassRef extends AbstractCElement implements NClass, CClass {

    private final String fullyQualifiedClassName;

    /**
     *
     * @param decl
     *      The {@link BIClass} declaration that has {@link BIClass#getExistingClassRef()}
     */
    public CClassRef(Model model, XSComponent source, BIClass decl, CCustomizations customizations) {
        super(model, source, decl.getLocation(), customizations);
        fullyQualifiedClassName = decl.getExistingClassRef();
        assert fullyQualifiedClassName!=null;
    }

    public void setAbstract() {
        // assume that the referenced class is marked as abstract to begin with.
    }

    public boolean isAbstract() {
        // no way to find out for sure
        return false;
    }

    public NType getType() {
        return this;
    }

    /**
     * Cached for both performance and single identity.
     */
    private JClass clazz;

    public JClass toType(Outline o, Aspect aspect) {
        if(clazz==null)
            clazz = o.getCodeModel().ref(fullyQualifiedClassName);
        return clazz;
    }

    public String fullName() {
        return fullyQualifiedClassName;
    }

    public QName getTypeName() {
        return null;
    }

// are these going to bite us?
//    we can compute some of them, but not all.

    public CElement getSubstitutionHead() {
        return null;
    }

    public CClassInfo getScope() {
        return null;
    }

    public QName getElementName() {
        return null;
    }

    public boolean isBoxedType() {
        return false;
    }

    public boolean isSimpleType() {
        return false;
    }

    
}
