package com.sun.tools.jxc.apt;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Wrapper class that will invoke the  {@link SchemaGenerator} and should
 * be called by the schemagen scripts
 *
 *
 * @author Bhakti Mehta
 */
public class SchemaGeneratorWrapper  {


    public SchemaGeneratorWrapper() {
    }

    public static void main (String args[]) {
        try {
            for (String arg : args) {
                if (arg.equals("-help")) {
                    usage();
                    System.exit(0);
                }
            }

            Class apt = null;
            try {
                apt = Class.forName("com.sun.tools.apt.Main");
            } catch (ClassNotFoundException e) {
                //Most likely this may not get executed as tools.jar
                //is there in the classpath when invoked from schemagen.sh
                //Just to make it more fool proof
                apt = getClassLoader().loadClass("com.sun.tools.apt.Main");
            }

            Method processMethod = apt.getMethod("process",
                    new Class[]{AnnotationProcessorFactory.class,String[].class});
            System.exit((Integer)processMethod.invoke(null,new Object[]{SchemaGenerator.class.newInstance(),args}));
        } catch (Exception e) {
            throw new AssertionError ("Unable to invoke the SchemaGenerator" + e);
        }

    }

    /**
     * Returns a class loader that can load classes from JDK tools.jar.
     *
     */
    private static ClassLoader  getClassLoader() {
        File jreHome = new File(System.getProperty("java.home"));
        File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );
        System.out.println("toolsJar" +toolsJar);
        URLClassLoader urlClassLoader ;
        try {
            urlClassLoader = new URLClassLoader(
                    new URL[]{ toolsJar.toURL() } );
            return urlClassLoader;
        } catch (MalformedURLException e) {
            throw new Error(e);
        }

    }

    private static void usage( ) {
        System.out.println(Messages.USAGE.format());
    }

}
