package com.sun.xml.bind.v2.model.impl;

import static com.sun.xml.bind.v2.model.core.ID.IDREF;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
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
class RuntimeAttributePropertyInfoImpl extends AttributePropertyInfoImpl<Type,Class,Field,Method>
    implements RuntimeAttributePropertyInfo {

    RuntimeAttributePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type,Class,Field,Method> seed) {
        super(classInfo, seed);
    }

    public boolean elementOnlyContent() {
        return true;
    }

    public RuntimeNonElement getTarget() {
        return (RuntimeNonElement) super.getTarget();
    }

    public List<? extends RuntimeNonElement> ref() {
        return (List<? extends RuntimeNonElement>)super.ref();
    }

    public RuntimePropertyInfo getSource() {
        return this;
    }

    public void link() {
        getTransducer();
        super.link();
    }
}
