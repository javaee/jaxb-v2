package com.sun.xml.bind.v2.runtime.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.Coordinator;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.reflect.opt.OptimizedAccessorFactory;

import org.xml.sax.SAXException;

/**
 * Accesses a particular property of a bean.
 *
 * <p>
 * This interface encapsulates the access to the actual data store.
 * The intention is to generate implementations for a particular bean
 * and a property to improve the performance.
 *
 * <p>
 * Accessor can be used as a receiver. Upon receiving an object
 * it sets that to the field.
 *
 * @see Accessor.FieldReflection
 * @see TransducedAccessor
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public abstract class Accessor<BeanT,ValueT> implements Receiver {

    public final Class<ValueT> valueType;

    public Class<ValueT> getValueType() {
        return valueType;
    }

    protected Accessor(Class<ValueT> valueType) {
        this.valueType = valueType;
    }

    /**
     * Returns the optimized version of the same accessor.
     *
     * @return
     *      At least the implementation can return <tt>this</tt>.
     */
    public Accessor<BeanT,ValueT> optimize() {
        return this;
    }


    /**
     * Gets the value of the property of the given bean object.
     *
     * @param bean
     *      must not be null.
     * @throws AccessorException
     *      if failed to set a value. For example, the getter method
     *      may throw an exception.
     *
     * @since 2.0 EA1
     */
    public abstract ValueT get(BeanT bean) throws AccessorException;

    /**
     * Sets the value of the property of the given bean object.
     *
     * @param bean
     *      must not be null.
     * @param value
     *      the value to be set. Setting value to null means resetting
     *      to the VM default value (even for primitive properties.)
     * @throws AccessorException
     *      if failed to set a value. For example, the setter method
     *      may throw an exception.
     *
     * @since 2.0 EA1
     */
    public abstract void set(BeanT bean,ValueT value) throws AccessorException;


    /**
     * Sets the value without adapting the value.
     *
     * This ugly entry point is only used by JAX-WS.
     * See {@link JAXBRIContext#getElementPropertyAccessor}
     */
    public Object getUnadapted(BeanT bean) throws AccessorException {
        return get(bean);
    }

    /**
     * Sets the value without adapting the value.
     *
     * This ugly entry point is only used by JAX-WS.
     * See {@link JAXBRIContext#getElementPropertyAccessor}
     */
    public void setUnadapted(BeanT bean,Object value) throws AccessorException {
        set(bean,(ValueT)value);
    }

    public void receive(UnmarshallingContext.State state, Object o) throws SAXException {
        try {
            set((BeanT)state.target,(ValueT)o);
        } catch (AccessorException e) {
            Loader.handleGenericException(e,true);
        }
    }

    /**
     * Wraps this  {@link Accessor} into another {@link Accessor}
     * and performs the type adaption as necessary.
     */
    public final <T> Accessor<BeanT,T> adapt(Class<T> targetType, final Class<? extends XmlAdapter<T,ValueT>> adapter) {
        final Accessor<BeanT,ValueT> extThis = this;

        return new Accessor<BeanT,T>(targetType) {
            public T get(BeanT bean) throws AccessorException {
                ValueT v = extThis.get(bean);
                if(v==null) return null;

                XmlAdapter<T,ValueT> a = getAdapter();
                try {
                    return a.marshal(v);
                } catch (Exception e) {
                    throw new AccessorException(e);
                }
            }

            public void set(BeanT bean, T o) throws AccessorException {
                if(o==null)
                    extThis.set(bean,null);
                else {
                    XmlAdapter<T, ValueT> a = getAdapter();
                    try {
                        extThis.set(bean,a.unmarshal(o));
                    } catch (Exception e) {
                        throw new AccessorException(e);
                    }
                }
            }

            public Object getUnadapted(BeanT bean) throws AccessorException {
                return extThis.getUnadapted(bean);
            }

            public void setUnadapted(BeanT bean, Object value) throws AccessorException {
                extThis.setUnadapted(bean,value);
            }

            /**
             * Sometimes Adapters are used directly by JAX-WS outside any
             * {@link Coordinator}. Use this lazily-created cached
             * {@link XmlAdapter} in such cases.
             */
            private XmlAdapter<T, ValueT> staticAdapter;

            private XmlAdapter<T, ValueT> getAdapter() {
                Coordinator coordinator = Coordinator._getInstance();
                if(coordinator!=null)
                    return coordinator.getAdapter(adapter);
                else {
                    synchronized(this) {
                        if(staticAdapter==null)
                            staticAdapter = ClassFactory.create(adapter);
                    }
                    return staticAdapter;
                }
            }
        };
    }

    /**
     * Flag that will be set to true after issueing a warning
     * about the lack of permission to access non-public fields.
     */
    private static boolean accessWarned = false;


    /**
     * {@link Accessor} that uses Java reflection to access a field.
     */
    public static final class FieldReflection<BeanT,ValueT> extends Accessor<BeanT,ValueT> implements PrivilegedAction<Void> {
        public final Field f;

        private static final Logger logger = Logger.getLogger(FieldReflection.class.getName());

        // TODO: revisit. this is a security hole because this method can be used by anyone
        // to enable access to a field.
        public FieldReflection(Field f) {
            super((Class<ValueT>)f.getType());
            this.f = f;
            AccessController.doPrivileged(this);
        }

        public Void run() {
            int mod = f.getModifiers();
            if(!Modifier.isPublic(mod) || Modifier.isFinal(mod) || !Modifier.isPublic(f.getDeclaringClass().getModifiers())) {
                try {
                    f.setAccessible(true);
                } catch( SecurityException e ) {
                    if(!accessWarned)
                        // this happens when we don't have enough permission.
                        logger.log( Level.WARNING, Messages.UNABLE_TO_ACCESS_NON_PUBLIC_FIELD.format(
                            f.getDeclaringClass().getName(),
                            f.getName()),
                            e );
                    accessWarned = true;
                }
            }
            return null;
        }

        public ValueT get(BeanT bean) {
            try {
                return (ValueT)f.get(bean);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }

        public void set(BeanT bean, ValueT value) {
            try {
                if(value==null)
                    value = (ValueT)uninitializedValues.get(valueType);
                f.set(bean,value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }

        @Override
        public Accessor<BeanT,ValueT> optimize() {
            Accessor acc = OptimizedAccessorFactory.get(f);
            if(acc!=null)
                return acc;
            else
                return this;
        }
    }


    /**
     * {@link Accessor} that uses Java reflection to access a getter and a setter.
     */
    public static final class GetterSetterReflection<BeanT,ValueT> extends Accessor<BeanT,ValueT> {
        public final Method getter;
        public final Method setter;

        private static final Logger logger = Logger.getLogger(FieldReflection.class.getName());

        public GetterSetterReflection(Method getter, Method setter) {
            super(
                (Class<ValueT>)( getter!=null ?
                    getter.getReturnType() :
                    setter.getParameterTypes()[0] ));
            this.getter = getter;
            this.setter = setter;

            if(getter!=null)
                makeAccessible(getter);
            if(setter!=null)
                makeAccessible(setter);
        }

        private void makeAccessible(Method m) {
            if(!Modifier.isPublic(m.getModifiers()) || !Modifier.isPublic(m.getDeclaringClass().getModifiers())) {
                try {
                    m.setAccessible(true);
                } catch( SecurityException e ) {
                    if(!accessWarned)
                        // this happens when we don't have enough permission.
                        logger.log( Level.WARNING, Messages.UNABLE_TO_ACCESS_NON_PUBLIC_FIELD.format(
                            m.getDeclaringClass().getName(),
                            m.getName()),
                            e );
                    accessWarned = true;
                }
            }
        }

        public ValueT get(BeanT bean) throws AccessorException {
            try {
                return (ValueT)getter.invoke(bean);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (InvocationTargetException e) {
                throw handleInvocationTargetException(e);
            }
        }

        public void set(BeanT bean, ValueT value) throws AccessorException {
            try {
                if(value==null)
                    value = (ValueT)uninitializedValues.get(valueType);
                setter.invoke(bean,value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (InvocationTargetException e) {
                throw handleInvocationTargetException(e);
            }
        }

        private AccessorException handleInvocationTargetException(InvocationTargetException e) {
            // don't block a problem in the user code
            Throwable t = e.getTargetException();
            if(t instanceof RuntimeException)
                throw (RuntimeException)t;
            if(t instanceof Error)
                throw (Error)t;

            // otherwise it's a checked exception.
            // I'm not sure how to handle this.
            // we can throw a checked exception from here,
            // but because get/set would be called from so many different places,
            // the handling would be tedious.
            return new AccessorException(t);
        }

        @Override
        public Accessor<BeanT, ValueT> optimize() {
            if(getter==null || setter==null)
                // if we aren't complete, OptimizedAccessor won't always work
                return this;

            Accessor acc = OptimizedAccessorFactory.get(getter,setter);
            if(acc!=null)
                return acc;
            else
                return this;
        }
    }

    /**
     * Special {@link Accessor} used to recover from errors.
     */
    public static final Accessor ERROR = new Accessor(Object.class) {
        public Object get(Object o) {
            return null;
        }

        public void set(Object o, Object o1) {
        }
    };

    /**
     * {@link Accessor} for {@link JAXBElement#getValue()}.
     */
    public static final Accessor<JAXBElement,Object> JAXB_ELEMENT_VALUE = new Accessor<JAXBElement,Object>(Object.class) {
        public Object get(JAXBElement jaxbElement) {
            return jaxbElement.getValue();
        }

        public void set(JAXBElement jaxbElement, Object o) {
            jaxbElement.setValue(o);
        }
    };

    /**
     * Uninitialized map keyed by their classes.
     */
    private static final Map<Class,Object> uninitializedValues = new HashMap<Class, Object>();

    static {
/*
    static byte default_value_byte = 0;
    static boolean default_value_boolean = false;
    static char default_value_char = 0;
    static float default_value_float = 0;
    static double default_value_double = 0;
    static int default_value_int = 0;
    static long default_value_long = 0;
    static short default_value_short = 0;
*/
        uninitializedValues.put(byte.class,Byte.valueOf((byte)0));
        uninitializedValues.put(boolean.class,false);
        uninitializedValues.put(char.class,Character.valueOf((char)0));
        uninitializedValues.put(float.class,Float.valueOf(0));
        uninitializedValues.put(double.class,Double.valueOf(0));
        uninitializedValues.put(int.class,Integer.valueOf(0));
        uninitializedValues.put(long.class,Long.valueOf(0));
        uninitializedValues.put(short.class,Short.valueOf((short)0));
    }
}
