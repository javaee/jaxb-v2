package com.sun.tools.xjc.model;

import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XmlString;

/**
 * Object that computes the default value expression lazily.
 *
 * The computation is done lazily because often the default value
 * needs to refer to things (such as enum classes) that are only generated
 * after some of the outline is built.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class CDefaultValue {
    public abstract JExpression compute(Outline outline);

    /**
     * Creates a new {@link CDefaultValue} that computes the default value
     * by applying a lexical representation to a {@link TypeUse}.
     */
    public static CDefaultValue create(final TypeUse typeUse, final XmlString defaultValue) {
        return new CDefaultValue() {
            public JExpression compute(Outline outline) {
                return typeUse.createConstant(outline,defaultValue);
            }
        };
    }
}
