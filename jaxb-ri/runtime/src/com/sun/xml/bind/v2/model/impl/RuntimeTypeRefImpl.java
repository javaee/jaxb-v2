package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Type;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.Transducer;

/**
 * @author Kohsuke Kawaguchi
 */
final class RuntimeTypeRefImpl extends TypeRefImpl<Type,Class> implements RuntimeTypeRef {

    public RuntimeTypeRefImpl(RuntimeElementPropertyInfoImpl elementPropertyInfo, QName elementName, Type type, boolean isNillable, String defaultValue) {
        super(elementPropertyInfo, elementName, type, isNillable, defaultValue);
    }

    public RuntimeNonElement getTarget() {
        return (RuntimeNonElement)super.getTarget();
    }

    public Transducer getTransducer() {
        return getTarget().getTransducer();
    }

    public RuntimePropertyInfo getSource() {
        return (RuntimePropertyInfo)owner;
    }
}
