package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;
import java.util.Set;

import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeReferencePropertyInfo extends ReferencePropertyInfo<Type,Class>, RuntimePropertyInfo {
    Set<? extends RuntimeElement> getElements();
}
