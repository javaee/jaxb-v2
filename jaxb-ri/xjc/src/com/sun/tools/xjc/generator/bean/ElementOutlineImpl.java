package com.sun.tools.xjc.generator.bean;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ElementOutline;

/**
 * {@link ElementOutline} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
final class ElementOutlineImpl extends ElementOutline {
    private final BeanGenerator parent;

    public BeanGenerator parent() {
        return parent;
    }

    /*package*/ ElementOutlineImpl(BeanGenerator parent, CElementInfo ei) {
        super(ei,
              parent.getClassFactory().createClass(
                      parent.getContainer( ei.parent, Aspect.EXPOSED ), ei.shortName(), ei.location ));
        this.parent = parent;

        JCodeModel cm = parent.getCodeModel();

        implClass._extends(
            cm.ref(JAXBElement.class).narrow(
                ei.getContentInMemoryType().toType(parent,Aspect.EXPOSED).boxify()));

        if(ei.hasClass()) {
            JType implType = ei.getContentInMemoryType().toType(parent,Aspect.IMPLEMENTATION);
            JExpression declaredType = JExpr.cast(cm.ref(Class.class),implType.boxify().dotclass()); // why do we have to cast?
            JClass scope=null;
            if(ei.getScope()!=null)
                scope = parent.getClazz(ei.getScope()).implRef;
            JExpression scopeClass = scope==null?JExpr._null():scope.dotclass();

            // take this opportunity to generate a constructor in the element class
            JMethod cons = implClass.constructor(JMod.PUBLIC);
            cons.body().invoke("super")
                .arg(implClass.field(JMod.PROTECTED|JMod.FINAL|JMod.STATIC,QName.class,"NAME",createQName(cm,ei.getElementName())))
                .arg(declaredType)
                .arg(scopeClass)
                .arg(cons.param(implType,"value"));

        }
    }

    /**
     * Generates an expression that evaluates to "new QName(...)"
     */
    private JInvocation createQName(JCodeModel codeModel,QName name) {
        return JExpr._new(codeModel.ref(QName.class)).arg(name.getNamespaceURI()).arg(name.getLocalPart());
    }
}
