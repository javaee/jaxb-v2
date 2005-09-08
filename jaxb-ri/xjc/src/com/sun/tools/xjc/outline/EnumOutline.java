package com.sun.tools.xjc.outline;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CEnumLeafInfo;

/**
 * Outline object that provides per-{@link CEnumLeafInfo} information
 * for filling in methods/fields for a bean.
 *
 * This object can be obtained from {@link Outline}
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class EnumOutline {

    /**
     * This {@link EnumOutline} holds information about this {@link CEnumLeafInfo}.
     */
    public final CEnumLeafInfo target;

    /**
     * The generated enum class.
     */
    public final JDefinedClass clazz;

    /**
     * Constants.
     */
    public final List<EnumConstantOutline> constants = new ArrayList<EnumConstantOutline>();

    protected EnumOutline(CEnumLeafInfo target, JDefinedClass clazz) {
        this.target = target;
        this.clazz = clazz;
    }
}
