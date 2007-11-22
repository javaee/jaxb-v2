/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.xjc.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import com.sun.istack.Nullable;

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
     *      The parent class loader to delegate to. Null to indicate bootstrap classloader.
     */
    public APTClassLoader(@Nullable ClassLoader parent, String[] packagePrefixes) throws ToolsJarNotFoundException {
        super(getToolsJar(parent),parent);
        if(getURLs().length==0)
            // if tools.jar was found in our classloader, no need to create
            // a parallel classes
            this.packagePrefixes = new String[0];
        else
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

            // define package if not defined yet
            int i = name.lastIndexOf('.');
            if (i != -1) {
                String pkgname = name.substring(0, i);
                Package pkg = getPackage(pkgname);
                if(pkg==null)
                    definePackage(pkgname, null, null, null, null, null, null, null);
            }

            return defineClass(name,buf,0,buf.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name,e);
        }
    }

    /**
     * Returns a class loader that can load classes from JDK tools.jar.
     * @param parent
     */
    private static URL[] getToolsJar(@Nullable ClassLoader parent) throws ToolsJarNotFoundException {

        try {
            Class.forName("com.sun.tools.javac.Main",false,parent);
            Class.forName("com.sun.tools.apt.Main",false,parent);
            return new URL[0];
            // we can already load them in the parent class loader.
            // so no need to look for tools.jar.
            // this happens when we are run inside IDE/Ant, or
            // in Mac OS.
        } catch (ClassNotFoundException e) {
            // otherwise try to find tools.jar
        }

        File jreHome = new File(System.getProperty("java.home"));
        File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );

        if (!toolsJar.exists()) {
            throw new ToolsJarNotFoundException(toolsJar);
        }

        try {
            return new URL[]{toolsJar.toURL()};
        } catch (MalformedURLException e) {
            // impossible
            throw new AssertionError(e);
        }
    }
}

