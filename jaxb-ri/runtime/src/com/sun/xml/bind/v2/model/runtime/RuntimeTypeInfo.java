package com.sun.xml.bind.v2.model.runtime;

import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.runtime.Transducer;

/**
 * @author Kohsuke Kawaguchi
 */
public interface RuntimeTypeInfo extends TypeInfo<Type,Class> {
    /**
     * If the XML representation of this bean is just a text,
     * return a transducer that converts between the bean and XML.
     */
    Transducer getTransducer();
}
