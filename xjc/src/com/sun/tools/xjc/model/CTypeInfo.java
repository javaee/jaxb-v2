package com.sun.tools.xjc.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.xsom.XSComponent;

import org.relaxng.datatype.ValidationContext;

/**
 * {@link TypeInfo} at the compile-time.
 * Either {@link CClassInfo}, {@link CBuiltinLeafInfo}, or {@link CElementInfo}.
 *
 * <p>
 * This interface implements {@link TypeUse} so that a {@link CTypeInfo}
 * instance can be used as a {@link TypeUse} instance.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface CTypeInfo extends TypeInfo<NType,NClass>, TypeUse, CCustomizable {

    /**
     * Given a text in XML, generates a constant of the mapped Java class.
     *
     * @throws IllegalStateException
     *      if the class that this info represents isn't mapping to
     *      a text in XML.
     */
    JExpression createConstant(JCodeModel codeModel, String lexical, ValidationContext context);

    /**
     * Returns the {@link JClass} that represents the class being bound,
     * under the given {@link Outline}.
     *
     * @see NType#toType(Outline, Aspect)
     */
    JType toType(Outline o, Aspect aspect);
}
