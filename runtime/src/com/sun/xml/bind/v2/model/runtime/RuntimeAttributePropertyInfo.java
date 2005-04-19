package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeAttributePropertyInfo extends AttributePropertyInfo<Type,Class>, RuntimePropertyInfo, RuntimeNonElementRef {
    // refinement
    RuntimeNonElement getTarget();
}
