package com.sun.tools.jxc.apt;

import java.lang.reflect.Method;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Runner class that will invoke the 
 * {@link SchemaGenerator}
 *
 *
 * @author Bhakti Mehta
 */
public class SchemaGeneratorRunner  {


    public static void main(String[] args) throws Exception{
        ClassLoader cl =  SchemaGeneratorRunner.class.getClassLoader();
        Class apt = cl.loadClass("com.sun.tools.apt.Main");
        if (apt != null) {
            Method processMethod = apt.getMethod("process",
                       new Class[]{AnnotationProcessorFactory.class,String[].class});

           System.exit(((Integer)processMethod.invoke(null,new Object[] {new SchemaGenerator(),args})));

        }
    }
}


