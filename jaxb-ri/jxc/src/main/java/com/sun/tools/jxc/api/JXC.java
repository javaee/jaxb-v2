package com.sun.tools.jxc.api;

import com.sun.tools.xjc.api.JavaCompiler;
import com.sun.tools.jxc.api.impl.j2s.JavaCompilerImpl;

/**
 * User: Iaroslav Savytskyi
 * Date: 25/05/12
 */
public class JXC {
    /**
     * Gets a fresh {@link JavaCompiler}.
     *
     * @return
     *      always return non-null object.
     */
    public static JavaCompiler createJavaCompiler() {
        return new JavaCompilerImpl();
    }
}
