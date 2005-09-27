package com.sun.tools.xjc.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

/**
 * {@link ClassLoader} that loads APT and specified classes
 * both into the same classloader, so that they can reference each other.
 *
 * @author Bhakti Mehta
 * @since 2.0 beta
 */
public final class APTClassLoader extends URLClassLoader {
    /**
     * List of package prefixes we want to mask the
     * parent classLoader from loading
     */
    private final String[] packagePrefixes;

    /**
     *
     * @param packagePrefixes
     *      The package prefixes that are forced to resolve within this class loader.
     * @param parent
     *      The parent class loader to delegate to.
     */
    public APTClassLoader(ClassLoader parent, String[] packagePrefixes) throws ToolsJarNotFoundException {
        super(new URL[]{getToolsJar()},parent);
        this.packagePrefixes = packagePrefixes;
    }

    public Class loadClass(String className) throws ClassNotFoundException {
        for( String prefix : packagePrefixes ) {
            if (className.startsWith(prefix) ) {
                // we need to load those classes in this class loader
                // without delegation.
                return findClass(className);
            }
        }

        return super.loadClass(className);

    }

    protected Class findClass(String name) throws ClassNotFoundException {

        StringBuilder sb = new StringBuilder(name.length() + 6);
        sb.append(name.replace('.','/')).append(".class");

        InputStream is = getResourceAsStream(sb.toString());
        if (is==null)
            throw new ClassNotFoundException("Class not found" + sb);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while((len=is.read(buf))>=0)
                baos.write(buf,0,len);

            buf = baos.toByteArray();

            return defineClass(name,buf,0,buf.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name,e);
        }
    }

    /**
     * Returns a class loader that can load classes from JDK tools.jar.
     */
    private static URL getToolsJar() throws ToolsJarNotFoundException {
        File jreHome = new File(System.getProperty("java.home"));
        File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );

        if (!toolsJar.exists()) {
            throw new ToolsJarNotFoundException(toolsJar);
        }

        try {
            return toolsJar.toURL();
        } catch (MalformedURLException e) {
            // impossible
            throw new AssertionError(e);
        }
    }
}

