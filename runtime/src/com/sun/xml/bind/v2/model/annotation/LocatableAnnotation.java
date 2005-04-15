package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link Annotation} that also implements {@link Locatable}.
 *
 * @author Kohsuke Kawaguchi
 */
public class LocatableAnnotation implements InvocationHandler, Locatable, Location {
    private final Annotation core;

    private final Locatable upstream;

    /**
     * Wraps the annotation into a proxy so that the returned object will also implement
     * {@link Locatable}.
     */
    public static <A extends Annotation> A create( A annotation, Locatable parentSourcePos ) {
        if(annotation==null)    return null;
        Class<? extends Annotation> type = annotation.annotationType();
        if(quicks.containsKey(type)) {
            // use the existing proxy implementation if available
            return (A)quicks.get(type).newInstance(parentSourcePos,annotation);
        }

        // otherwise take the slow route
        // TODO: consider eliminating this.
        return (A)Proxy.newProxyInstance(LocatableAnnotation.class.getClassLoader(),
                new Class[]{ type, Locatable.class },
                new LocatableAnnotation(annotation,parentSourcePos));
    }

    LocatableAnnotation(Annotation core, Locatable upstream) {
        this.core = core;
        this.upstream = upstream;
    }

    public Locatable getUpstream() {
        return upstream;
    }

    public Location getLocation() {
        return this;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if(method.getDeclaringClass()==Locatable.class)
                return method.invoke(this,args);
            else
                return method.invoke(core,args);
        } catch (InvocationTargetException e) {
            if(e.getTargetException()!=null)
                throw e.getTargetException();
            throw e;
        }
    }

    public String toString() {
        return core.toString();
    }


    /**
     * List of {@link Quick} implementations keyed by their annotation type.
     */
    private static final Map<Class,Quick> quicks = new HashMap<Class, Quick>();

    static {
        for( Quick q : Init.getAll() ) {
            quicks.put(q.annotationType(),q);
        }
    }
}
