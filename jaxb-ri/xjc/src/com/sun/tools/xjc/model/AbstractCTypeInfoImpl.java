package com.sun.tools.xjc.model;

import java.util.List;
import java.util.Collections;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JCodeModel;

import org.relaxng.datatype.ValidationContext;

/**
 * Partial implementation of {@link CTypeInfo}.
 *
 * <p>
 * The inheritance of {@link TypeUse} by {@link CTypeInfo}
 * isn't a normal inheritance (see {@link CTypeInfo} for more.)
 * This class implments methods on {@link TypeUse} for {@link CTypeInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractCTypeInfoImpl implements CTypeInfo {

    private final List<CPluginCustomization> customizations;

    protected AbstractCTypeInfoImpl(List<CPluginCustomization> customizations) {
        if(customizations==null)
            customizations = Collections.emptyList();
        this.customizations = customizations;
    }

    public final boolean isCollection() {
        return false;
    }

    public final CAdapter getAdapterUse() {
        return null;
    }

    public final CTypeInfo getInfo() {
        return this;
    }

    public final ID idUse() {
        return ID.NONE;
    }

    public List<CPluginCustomization> getCustomizations() {
        return customizations;
    }

    // this is just a convenient default
    public JExpression createConstant(JCodeModel codeModel, String lexical, ValidationContext context) {
        return null;
    }
}
