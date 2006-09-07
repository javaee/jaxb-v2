package com.sun.tools.xjc.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.xsom.XmlString;

/**
 * {@link TypeInfo} at the compile-time.
 * Either {@link CClassInfo}, {@link CBuiltinLeafInfo}, or {@link CElementInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CTypeInfo extends TypeInfo<NType,NClass>, CCustomizable {

    /**
     * Returns the {@link JClass} that represents the class being bound,
     * under the given {@link Outline}.
     *
     * @see NType#toType(Outline, Aspect)
     */
    JType toType(Outline o, Aspect aspect);
}
