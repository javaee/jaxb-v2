package com.sun.xml.bind.v2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.xml.bind.Util;

/**
 * Creates new instances of classes.
 *
 * <p>
 * This code handles the case where the class is not public or the constructor is
 * not public.
 *
 * @since 2.0
 * @author Kohsuke Kawaguchi
 */
public final class ClassFactory {
    private static final Class[] emptyClass = new Class[0];
    private static final Object[] emptyObject = new Object[0];

    private static final Logger logger = Util.getClassLogger();

    /**
     * Cache from a class to its default constructor.
     *
     * To avoid synchronization among threads, we use {@link ThreadLocal}.
     */
    private static final ThreadLocal<Map<Class,Constructor>> tls = new ThreadLocal<Map<Class,Constructor>>() {
        public Map<Class, Constructor> initialValue() {
            return new WeakHashMap<Class,Constructor>();
        }
    };

    /**
     * Creates a new instance of the class but throw exceptions without catching it.
     */
    public static <T> T create0( final Class<T> clazz ) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<Class,Constructor> m = tls.get();
        Constructor<T> cons = m.get(clazz);
        if(cons==null) {
            cons = AccessController.doPrivileged(new PrivilegedAction<Constructor<T>>() {
                public Constructor<T> run() {
                    Constructor<T> cons;

                    try {
                        cons = clazz.getDeclaredConstructor(emptyClass);
                    } catch (NoSuchMethodException e) {
                        logger.log(Level.INFO,"No default constructor found on "+clazz,e);
                        throw new NoSuchMethodError(e.getMessage());
                    }

                    int classMod = clazz.getModifiers();

                    if(!Modifier.isPublic(classMod) || !Modifier.isPublic(cons.getModifiers())) {
                        // attempt to make it work even if the constructor is not accessible
                        try {
                            cons.setAccessible(true);
                        } catch(SecurityException e) {
                            // but if we don't have a permission to do so, work gracefully.
                            logger.log(Level.FINE,"Unable to make the constructor of "+clazz+" accessible",e);
                            throw e;
                        }
                    }

                    return cons;
                }
            });

            m.put(clazz,cons);
        }

        return cons.newInstance(emptyObject);
    }

    /**
     * The same as {@link #create0} but with an error handling to make
     * the instanciation error fatal.
     */
    public static <T> T create( Class<T> clazz ) {
        try {
            return create0(clazz);
        } catch (InstantiationException e) {
            logger.log(Level.INFO,"failed to create a new instance of "+clazz,e);
            throw new InstantiationError(e.toString());
        } catch (IllegalAccessException e) {
            logger.log(Level.INFO,"failed to create a new instance of "+clazz,e);
            throw new IllegalAccessError(e.toString());
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();

            // most likely an error on the user's code.
            // just let it through for the ease of debugging
            if(target instanceof RuntimeException)
                throw (RuntimeException)target;

            // error. just forward it for the ease of debugging
            if(target instanceof Error)
                throw (Error)target;

            // a checked exception.
            // not sure how we should report this error,
            // but for now we just forward it by wrapping it into a runtime exception
            throw new IllegalStateException(target);
        }
    }
}
