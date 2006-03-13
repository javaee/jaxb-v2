package com.sun.tools.xjc;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * {@link ClassLoader} that masks some packages available in the parent class loader.
 * @author Kohsuke Kawaguchi
 */
public class MaskingClassLoader extends ClassLoader {

    private final String[] prefixList;

    public MaskingClassLoader(ClassLoader parent, List prefixList) {
        super(parent);
        this.prefixList = (String[]) prefixList.toArray(new String[prefixList.size()]);
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if(matchesPrefix(name))
            // we are hiding these packages from its parent.
            throw new ClassNotFoundException();
        return super.loadClass(name,resolve);
    }

    public URL getResource(String name) {
        if(matchesPrefix(name))
            return null;
        return super.getResource(name);
    }

    public Enumeration getResources(String name) throws IOException {
        if(matchesPrefix(name))
            return new Vector().elements();
        return super.getResources(name);
    }

    private boolean matchesPrefix(String name) {
        for (int i = 0; i < prefixList.length; i++ ) {
            String packprefix = prefixList[i];
            if (name.startsWith(packprefix) )
                return true;
        }
        return false;
    }


}
