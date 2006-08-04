package com.sun.xml.bind.v2.runtime;

import java.lang.reflect.Method;

import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

/**
 * This class is a simple container for caching lifecycle methods that are
 * discovered during construction of (@link JAXBContext}.
 * 
 * @see JaxBeanInfo#lcm
 * @see Loader#fireBeforeUnmarshal(JaxBeanInfo, Object, UnmarshallingContext.State)
 * @see Loader#fireAfterUnmarshal(JaxBeanInfo, Object, UnmarshallingContext.State) 
 * @see XMLSerializer#fireMarshalEvent(Object, Method)
 */
final class LifecycleMethods {
    Method beforeUnmarshal;
    Method afterUnmarshal;
    Method beforeMarshal;
    Method afterMarshal;
}