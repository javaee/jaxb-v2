package com.sun.tools.jxc.apt;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/**
 * This classloader invoked by the  {@link SchemaGeneratorWrapper} is responsible
 * for masking classes which are loaded by the parent class loader so that the child classloader
 * classes living in different jars are loaded  before the parent class loader 
 * loads classes
 *
 *
 * @author Bhakti Mehta
 */
public class SchemaGeneratorClassLoader extends URLClassLoader {
    /**
     * List of package prefixes we want to mask the
     * parent classLoader from loading
     */
    private static final String[] packagePrefixes = {
        "com.sun.tools.jxc.",
        "com.sun.tools.xjc.",
        "com.sun.tools.apt.",
        "com.sun.tools.javac.",
        "com.sun.mirror."
    };

    protected SchemaGeneratorClassLoader(ClassLoader parent,URL... urls) {
        super(urls,parent);
    }

    public Class loadClass (String s) throws ClassNotFoundException {
        for( String prefix : packagePrefixes ) {
            if (s.startsWith(prefix) ) {
                return findClass(s);
            }
        }
        return getParent().loadClass(s);
    }



    protected Class findClass(String name) throws ClassNotFoundException {

        StringBuffer sb = new StringBuffer(name.length() + 6);
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
}

