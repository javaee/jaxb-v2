package com.sun.xml.bind.v2.runtime.reflect.opt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.bind.Util;

/**
 * A {@link ClassLoader} used to "inject" optimized accessor classes
 * into the VM.
 *
 * <p>
 * Its parent class loader needs to be set to the one that can see the user
 * class.
 *
 * @author Kohsuke Kawaguchi
 */
final class Injector {

    /**
     * {@link Injector}s keyed by their parent {@link ClassLoader}.
     *
     * We only need one injector per one user class loader.
     */
    private static final Map<ClassLoader,Injector> injectors =
        Collections.synchronizedMap(new WeakHashMap<ClassLoader,Injector>());

    private static final Logger logger = Util.getClassLogger();

    /**
     * Injects a new class into the given class loader.
     *
     * @return null
     *      if it fails to inject.
     */
    static Class inject( ClassLoader cl, String className, byte[] image ) {
        Injector injector = get(cl);
        if(injector!=null)
            return injector.inject(className,image);
        else
            return null;
    }

    /**
     * Returns the already injected class, or null.
     */
    static Class find( ClassLoader cl, String className ) {
        Injector injector = get(cl);
        if(injector!=null)
            return injector.find(className);
        else
            return null;
    }

    /**
     * Gets or creates an {@link Injector} for the given class loader.
     *
     * @return null
     *      if it fails.
     */
    private static Injector get(ClassLoader cl) {
        Injector injector = injectors.get(cl);
        if(injector==null)
            try {
                injectors.put(cl,injector = new Injector(cl));
            } catch (SecurityException e) {
                logger.log(Level.FINE,"Unable to set up a back-door for the injector",e);
                return null;
            }
        return injector;
    }

    /**
     * Injected classes keyed by their names.
     */
    private final Map<String,Class> classes = new HashMap<String,Class>();

    private final ClassLoader parent;

    private static final Method defineClass;
    private static final Method resolveClass;

    static {
        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass",String.class,byte[].class,Integer.TYPE,Integer.TYPE);
            resolveClass = ClassLoader.class.getDeclaredMethod("resolveClass",Class.class);
        } catch (NoSuchMethodException e) {
            // impossible
            throw new NoSuchMethodError(e.getMessage());
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                // TODO: check security implication
                // do these setAccessible allow anyone to call these methods freely?s
                defineClass.setAccessible(true);
                resolveClass.setAccessible(true);
                return null;
            }
        });
    }

    private Injector(ClassLoader parent) {
        this.parent = parent;
        assert parent!=null;
    }


    private synchronized Class inject(String className, byte[] image) {
        Class c = classes.get(className);
        if(c==null) {
            // we need to inject a class into the
            try {
                c = (Class)defineClass.invoke(parent,className.replace('/','.'),image,0,image.length);
                resolveClass.invoke(parent,c);
            } catch (IllegalAccessException e) {
                logger.log(Level.FINE,"Unable to inject "+className,e);
                return null;
            } catch (InvocationTargetException e) {
                logger.log(Level.FINE,"Unable to inject "+className,e);
                return null;
            } catch (SecurityException e) {
                logger.log(Level.FINE,"Unable to inject "+className,e);
                return null;
            }
            classes.put(className,c);
        }
        return c;
    }

    private synchronized Class find(String className) {
        return classes.get(className);
    }
}
