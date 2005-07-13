package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.MapPropertyInfo;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeMapPropertyInfo extends RuntimePropertyInfo, MapPropertyInfo<Type,Class> {
    RuntimeNonElement getKeyType();
    RuntimeNonElement getValueType();
}
