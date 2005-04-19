package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import javax.activation.MimeType;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl;
import com.sun.xml.bind.v2.runtime.reflect.Lister;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

/**
 * @author Kohsuke Kawaguchi
 */
final class RuntimeValuePropertyInfoImpl extends ValuePropertyInfoImpl<Type,Class,Field,Method>
    implements RuntimeValuePropertyInfo {

    RuntimeValuePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type,Class,Field,Method> seed) {
        super(classInfo, seed);
    }

    public boolean elementOnlyContent() {
        return false;
    }

    public RuntimePropertyInfo getSource() {
        return (RuntimePropertyInfo)super.getSource();
    }

    public RuntimeNonElement getTarget() {
        return (RuntimeNonElement)super.getTarget();
    }

    public List<? extends RuntimeNonElement> ref() {
        return (List<? extends RuntimeNonElement>)super.ref();
    }

    public void link() {
        getTransducer();
        super.link();
    }
}
