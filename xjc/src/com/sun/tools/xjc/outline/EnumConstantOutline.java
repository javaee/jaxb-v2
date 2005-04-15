package com.sun.tools.xjc.outline;

import com.sun.codemodel.JEnumConstant;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.generator.bean.BeanGenerator;

/**
 * Outline object that provides per-{@link CEnumConstant} information.
 *
 * This object can be obtained from {@link EnumOutline}
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class EnumConstantOutline {
    /**
     * This {@link EnumOutline} holds information about this {@link CEnumLeafInfo}.
     */
    public final CEnumConstant target;

    /**
     * The generated enum constant.
     */
    public final JEnumConstant constRef;

    /**
     * Reserved for {@link BeanGenerator}.
     */
    protected EnumConstantOutline(CEnumConstant target, JEnumConstant constRef) {
        this.target = target;
        this.constRef = constRef;
    }
}
