package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

/**
 * @author Kohsuke Kawaguchi
 */
public interface NClass extends NType {
    JClass toType(Outline o, Aspect aspect);

    boolean isAbstract();
}
