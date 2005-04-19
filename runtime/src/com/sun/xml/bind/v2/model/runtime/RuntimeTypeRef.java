package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.TypeRef;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeTypeRef extends TypeRef<Type,Class>, RuntimeNonElementRef {
    RuntimeNonElement getTarget();
}
