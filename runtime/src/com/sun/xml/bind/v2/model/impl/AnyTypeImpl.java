package com.sun.xml.bind.v2.model.impl;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.WellKnownNamespace;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;

/**
 * {@link TypeInfo} implementation for <tt>xs:anyType</tt>.
 *
 * @author Kohsuke Kawaguchi
 */
public class AnyTypeImpl<T,C> implements NonElement<T,C> {

    private final T type;

    public AnyTypeImpl(Navigator<T,C,?,?> nav) {
        this.type = nav.ref(Object.class);
    }

    public QName getTypeName() {
        return name;
    }

    public T getType() {
        return type;
    }


    private static final QName name = new QName(WellKnownNamespace.XML_SCHEMA,"anyType");
}
