package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.Element;

/**
 * Either {@link CElementInfo} or {@link CClassInfo}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CElement extends CTypeInfo, Element<NType,NClass>, CClassInfoParent {
    /**
     * Marks this element as an abstract element.
     */
    void setAbstract();

    /**
     * Returns true iff this element is an abstract element.
     */
    boolean isAbstract();
}
