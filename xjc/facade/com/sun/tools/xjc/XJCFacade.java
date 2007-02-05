package com.sun.tools.xjc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;

import com.sun.istack.tools.MaskingClassLoader;
import com.sun.istack.tools.ParallelWorldClassLoader;

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
            ClassLoader cl = createProtectiveClassLoader(XJCFacade.class.getClassLoader(), v);

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

    /**
     * Creates a new class loader that eventually delegates to the given {@link ClassLoader}
     * such that XJC can be loaded by using this classloader.
     *
     * @param v
     *      Either "1.0" or "2.0", indicating the version of the -source value.
     */
    protected static ClassLoader createProtectiveClassLoader(ClassLoader cl, String v) throws ClassNotFoundException, MalformedURLException {
        if(noHack)  return cl;  // provide an escape hatch

        boolean mustang = false;

        if(JAXBContext.class.getClassLoader()==null) {
            // JAXB API is loaded from the bootstrap. We need to override one with ours
            mustang = true;

            List mask = new ArrayList(Arrays.asList(maskedPackages));
            mask.add("javax.xml.bind.");

            cl = new MaskingClassLoader(cl,mask);

            URL apiUrl = cl.getResource("javax/xml/bind/annotation/XmlSeeAlso.class");
            if(apiUrl==null)
                throw new ClassNotFoundException("There's no JAXB 2.1 API in the classpath");

            cl = new URLClassLoader(new URL[]{ParallelWorldClassLoader.toJarUrl(apiUrl)},cl);
        }

        //Leave XJC2 in the publicly visible place
        // and then isolate XJC1 in a child class loader,
        // then use a MaskingClassLoader
        // so that the XJC2 classes in the parent class loader
        //  won't interfere with loading XJC1 classes in a child class loader

        if (v.equals("1.0")) {
            if(!mustang)
                // if we haven't used Masking ClassLoader, do so now.
                cl = new MaskingClassLoader(cl,maskedPackages);
            cl = new ParallelWorldClassLoader(cl,"1.0/");
        } else {
            if(mustang)
                // the whole RI needs to be loaded in a separate class loader
                cl = new ParallelWorldClassLoader(cl,"");
        }

        return cl;
    }

    private static String parseVersion(String version) {
        if(version.equals("1.0"))
            return version;
        // if we don't recognize the version number, we'll go to 2.0 RI
        // anyway. It's easier to report an error message there,
        // than in here.
        return "2.0";
    }

    /**
     * The list of package prefixes we want the
     * {@link MaskingClassLoader} to prevent the parent
     * classLoader from loading
     */
    private static String[] maskedPackages = new String[]{
        "com.sun.tools.",
        "com.sun.codemodel.",
        "com.sun.relaxng.",
        "com.sun.xml.xsom.",
        "com.sun.xml.bind.",
    };

    /**
     * Escape hatch in case this class loader hack breaks.
     */
    public static final boolean noHack = Boolean.getBoolean(XJCFacade.class.getName()+".nohack");
}
