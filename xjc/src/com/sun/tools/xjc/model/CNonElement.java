package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.NonElement;

/**
 * {@link NonElement} at compile-time.
 *
 * <p>
 * This interface implements {@link TypeUse} so that a {@link CNonElement}
 * instance can be used as a {@link TypeUse} instance.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CNonElement extends NonElement<NType,NClass>, TypeUse, CTypeInfo {
    /**
     * Guaranteed to return this.
     */
    @Deprecated
    CNonElement getInfo();

    /**
     * Guaranteed to return false.
     */
    @Deprecated
    boolean isCollection();

    /**
     * Guaranteed to return null.
     */
    @Deprecated
    CAdapter getAdapterUse();
}
