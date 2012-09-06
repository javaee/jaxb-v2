package com.sun.xml.bind.util;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * User: Iaroslav Savytskyi
 * Date: 23/05/12
 */
public class ImplementationContainer {

    private static final ImplementationContainer INSTANCE = new ImplementationContainer();

    private volatile ImplementationFactory implementationFactory;

    private ImplementationContainer() {}

    public static ImplementationContainer getInstance() {
        return INSTANCE;
    }

    public ImplementationFactory getImplementationFactory() {
        if (implementationFactory == null) {
            synchronized (this) {
                if (implementationFactory == null) {
                    implementationFactory = getImplementation();
                    if (implementationFactory == null) {
                        throw new NullPointerException("No implementation found. Please add one.");
                    }
                }
            }
        }
        return implementationFactory;
    }

    private ImplementationFactory getImplementation() {
        ClassLoader ucl = SecureLoader.getClassClassLoader(getClass());
        Iterator<ImplementationFactory> it = ServiceLoader.load(ImplementationFactory.class, ucl).iterator();
        ImplementationFactory newFactory = null;
        if (it.hasNext()) {
            newFactory = it.next();
            if (it.hasNext())
                throw new IllegalStateException("more than one implementation found");
        }
        return newFactory;
    }
}
