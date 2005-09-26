package com.sun.tools.jxc.apt;

import java.lang.reflect.Method;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Runner that will actually invoke the {@link SchemaGenerator}.
 *
 * <p>
 * This code runs within a classloader that can load both APT and JAXB.
 *
 * @author Bhakti Mehta
 */
public final class SchemaGeneratorRunner {
    public static int main(String[] args) throws Exception {
        ClassLoader cl = SchemaGeneratorRunner.class.getClassLoader();
        Class apt = cl.loadClass("com.sun.tools.apt.Main");
        Method processMethod = apt.getMethod("process",
                new Class[]{AnnotationProcessorFactory.class, String[].class});

        return (Integer) processMethod.invoke(null, new SchemaGenerator(), args);
    }
}


