package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.NonElement;

/**
 * @author Kohsuke Kawaguchi
 */
public interface CNonElement extends NonElement<NType,NClass>, CTypeInfo {
}
