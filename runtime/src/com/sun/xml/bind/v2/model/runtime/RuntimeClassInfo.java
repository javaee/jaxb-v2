package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface RuntimeClassInfo extends ClassInfo<Type,Class>, RuntimeNonElement {
    RuntimeClassInfo getBaseClass();

    // refined to return RuntimePropertyInfo
    List<? extends RuntimePropertyInfo> getProperties();

    /**
     * If {@link #hasAttributeWildcard()} is true,
     * returns the accessor to access the property.
     *
     * @return
     *      unoptimized accessor.
     *      non-null iff {@link #hasAttributeWildcard()}==true.
     *
     * @see Accessor#optimize()
     */
    Accessor<?,Map<QName,Object>> getAttributeWildcard();
}
