package com.sun.tools.xjc.outline;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CElementInfo;

/**
 * Outline object that provides per-{@link CElementInfo} information
 * for filling in methods/fields for a bean.
 *
 * This interface is accessible from {@link Outline}. This object is
 * not created for all {@link CElementInfo}s.
 * It is only for those {@link CElementInfo} that has a class.
 * (IOW, {@link CElementInfo#hasClass()}
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class ElementOutline {

    /**
     * A {@link Outline} that encloses all the class outlines.
     */
    public abstract Outline parent();

    /**
     * {@link PackageOutline} that contains this class.
     */
    public PackageOutline _package() {
        return parent().getPackageContext(implClass._package());
    }

    /**
     * This {@link ElementOutline} holds information about this {@link CElementInfo}.
     */
    public final CElementInfo target;

    /**
     * The implementation aspect of a bean.
     * The actual place where fields/methods should be generated into.
     */
    public final JDefinedClass implClass;


    protected ElementOutline(CElementInfo target, JDefinedClass implClass) {
        this.target = target;
        this.implClass = implClass;
    }
}
