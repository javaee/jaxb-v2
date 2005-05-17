package com.sun.xml.bind.v2.runtime.reflect.opt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;

import static com.sun.xml.bind.v2.bytecode.ClassTailor.toVMClassName;
import static com.sun.xml.bind.v2.bytecode.ClassTailor.toVMTypeName;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class OptimizedAccessorFactory {
    private OptimizedAccessorFactory() {} // no instanciation please

    private static final Logger logger = Util.getClassLogger();


    private static final String fieldTemplateName;
    private static final String methodTemplateName;

    static {
        String s = FieldAccessor_Byte.class.getName();
        fieldTemplateName = s.substring(0,s.length()-"Byte".length()).replace('.','/');

        s = MethodAccessor_Byte.class.getName();
        methodTemplateName = s.substring(0,s.length()-"Byte".length()).replace('.','/');
    }

    /**
     * Gets the optimized {@link Accessor} that accesses the given getter/setter.
     *
     * @return null
     *      if for some reason it fails to create an optimized version.
     */
    public static final Accessor get(Method getter, Method setter) {
        // make sure the method signatures are what we expect
        if(getter.getParameterTypes().length!=0)
            return null;
        Class<?>[] sparams = setter.getParameterTypes();
        if(sparams.length!=1)
            return null;
        if(sparams[0]!=getter.getReturnType())
            return null;
        if(setter.getReturnType()!=Void.TYPE)
            return null;
        if(getter.getDeclaringClass()!=setter.getDeclaringClass())
            return null;
        if(Modifier.isPrivate(getter.getModifiers()) || Modifier.isPrivate(setter.getModifiers()))
            // we can't access private fields
            return null;


        Class t = sparams[0];
        String typeName = t.getName().replace('.','_');

        String newClassName = toVMClassName(getter.getDeclaringClass())+"$JaxbAccessorM_"+getter.getName()+'_'+setter.getName()+'_'+typeName;

        Class opt;

        if(t.isPrimitive())
            opt = AccessorInjector.prepare( getter.getDeclaringClass(),
                methodTemplateName+t.getName(),
                newClassName,
                toVMClassName(Bean.class),
                toVMClassName(getter.getDeclaringClass()),
                "get_"+t.getName(),
                getter.getName(),
                "set_"+t.getName(),
                setter.getName());
        else
            opt = AccessorInjector.prepare( getter.getDeclaringClass(),
                methodTemplateName+"Ref",
                newClassName,
                toVMClassName(Bean.class),
                toVMClassName(getter.getDeclaringClass()),
                toVMClassName(Ref.class),
                toVMClassName(t),
                "()"+toVMTypeName(Ref.class),
                "()"+toVMTypeName(t),
                '('+toVMTypeName(Ref.class)+")V",
                '('+toVMTypeName(t)+")V",
                "get_ref",
                getter.getName(),
                "set_ref",
                setter.getName());

        if(opt==null)
            return null;

        Accessor acc = instanciate(opt);
        if(acc!=null)
            logger.log(Level.FINE,"Using optimized Accessor for "+getter+" and "+setter);
        return acc;
    }


    /**
     * Gets the optimizd {@link Accessor} that accesses the given field.
     *
     * @return null
     *      if for some reason it fails to create an optimized version.
     */
    public static final Accessor get(Field field) {
        int mods = field.getModifiers();
        if(Modifier.isPrivate(mods) || Modifier.isFinal(mods))
            // we can't access private fields
            return null;

        String newClassName = toVMClassName(field.getDeclaringClass())+"$JaxbAccessorF_"+field.getName();

        Class opt;

        if(field.getType().isPrimitive())
            opt = AccessorInjector.prepare( field.getDeclaringClass(),
                fieldTemplateName+field.getType().getName(),
                newClassName,
                toVMClassName(Bean.class),
                toVMClassName(field.getDeclaringClass()),
                "f_"+field.getType().getName(),
                field.getName() );
        else
            opt = AccessorInjector.prepare( field.getDeclaringClass(),
                fieldTemplateName+"Ref",
                newClassName,
                toVMClassName(Bean.class),
                toVMClassName(field.getDeclaringClass()),
                toVMClassName(Ref.class),
                toVMClassName(field.getType()),
                toVMTypeName(Ref.class),
                toVMTypeName(field.getType()),
                "f_ref",
                field.getName() );

        if(opt==null)
            return null;

        Accessor acc = instanciate(opt);
        if(acc!=null)
            logger.log(Level.FINE,"Using optimized Accessor for "+field);
        return acc;
    }

    private static Accessor instanciate(Class opt) {
        try {
            return (Accessor)opt.newInstance();
        } catch (InstantiationException e) {
            logger.log(Level.INFO,"failed to load an optimized Accessor",e);
        } catch (IllegalAccessException e) {
            logger.log(Level.INFO,"failed to load an optimized Accessor",e);
        } catch (SecurityException e) {
            logger.log(Level.INFO,"failed to load an optimized Accessor",e);
        }
        return null;
    }
}
