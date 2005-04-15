package com.sun.xml.bind.v2.model.impl;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.Transducer;

/**
 * @author Kohsuke Kawaguchi
 */
public final class RuntimeAnyTypeImpl extends AnyTypeImpl<Type,Class> implements RuntimeNonElement {
    private RuntimeAnyTypeImpl() {
        super(Navigator.REFLECTION);
    }

    public static final RuntimeNonElement theInstance = new RuntimeAnyTypeImpl();

    public Transducer getTransducer() {
        return null;
    }
}
