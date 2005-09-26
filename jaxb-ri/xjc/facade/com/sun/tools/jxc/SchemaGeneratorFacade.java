package com.sun.tools.jxc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * CLI entry point to schemagen that checks for JDK 5.0
 * @author Kohsuke Kawaguchi
 */
public class SchemaGeneratorFacade {

    public static void main(String[] args) throws Throwable {
        try {
            ClassLoader cl = SchemaGeneratorFacade.class.getClassLoader();
            if(cl==null)    cl = ClassLoader.getSystemClassLoader();

            Class driver = cl.loadClass("com.sun.tools.jxc.SchemaGenerator");
            Method mainMethod = driver.getDeclaredMethod("main", new Class[]{String[].class});
            try {
                mainMethod.invoke(null,new Object[]{args});
            } catch (IllegalAccessException e) {
                throw e;
            } catch (InvocationTargetException e) {
                if(e.getTargetException()!=null)
                    throw e.getTargetException();
            }
        } catch (UnsupportedClassVersionError e) {
            System.err.println("schemagen requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
        }
    }
}
