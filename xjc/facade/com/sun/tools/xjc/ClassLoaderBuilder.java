package com.sun.tools.xjc;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;

import com.sun.istack.tools.MaskingClassLoader;
import com.sun.istack.tools.ParallelWorldClassLoader;

/**
 * Creates a class loader configured to run XJC 1.0/2.0 safely without
 * interference with JAXB 2.0 API in Mustang.
 *
 * @author Kohsuke Kawaguchi
 */
class ClassLoaderBuilder {

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
