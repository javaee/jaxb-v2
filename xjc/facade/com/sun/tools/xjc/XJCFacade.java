package com.sun.tools.xjc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A shabby driver to invoke XJC1 or XJC2 depending on the command line switch.
 *
 * <p>
 * This class is compiled with -source 1.2 so that we can report a nice user-friendly
 * "you require Tiger" error message.
 *
 * @author Kohsuke Kawaguchi
 */
public class XJCFacade {

    public static void main(String[] args) throws Throwable {
        String v = "2.0";      // by default, we go 2.0

        for( int i=0; i<args.length; i++ ) {
            if(args[i].equals("-source")) {
                if(i+1<args.length) {
                    v = parseVersion(args[i+1]);
                }
            }
        }

        try {
            ClassLoader cl = ClassLoaderBuilder.createProtectiveClassLoader(XJCFacade.class.getClassLoader(), v);

            Class driver = cl.loadClass("com.sun.tools.xjc.Driver");
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
            System.err.println("XJC requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
        }
    }

    private static String parseVersion(String version) {
        if(version.equals("1.0"))
            return version;
        // if we don't recognize the version number, we'll go to 2.0 RI
        // anyway. It's easier to report an error message there,
        // than in here.
        return "2.0";
    }
}
