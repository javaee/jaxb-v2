/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.tools.xjc.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletContext;

/**
 * Modified version of the {@link URLClassLoader} that tries
 * to load classes from itself before delegating to the parent.
 * 
 * This class loader is used to workaround the bug in JWSDP1.2.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XJCClassLoader extends URLClassLoader {
    
    private XJCClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

//    private XJCClassLoader(URL[] urls) {
//        super(urls);
//    }
//
//    private XJCClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
//        super(urls, parent, factory);
//    }
    
    private static XJCClassLoader theInstance;
    
    public static ClassLoader getInstance( ServletContext context ) throws IOException {
        if( theInstance==null ) {
            theInstance = new XJCClassLoader( new URL[] {
                context.getResource("/WEB-INF/lib/boxed.jar"),
                context.getResource("/WEB-INF/lib/jaxb-xjc.jar"),
                context.getResource("/WEB-INF/lib/jaxb-impl.jar") },
                Thread.currentThread().getContextClassLoader() );
        }
        
        return theInstance;
    }
    
    

    public URL getResource(String name) {
        // try this class loader first
        URL url = findResource(name);
        
        if( url==null ) {
            // if fails, delegate to the parent
            url = getParent().getResource(name);
        }
        return url;
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class c = findLoadedClass(name);
        if (c == null) {
            
            if( shouldDelegate(name) )
                c = getParent().loadClass(name);
            else {
                try {
                    // try this class loader first.
                    c = findClass(name);
//                    System.out.println("found "+name);
                } catch( ClassNotFoundException e ) {
                    // if fail, delegate to the parent
                    c = getParent().loadClass(name);
                }
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    private boolean shouldDelegate(String name) {
        if( name.startsWith("org.xml")
        ||  name.startsWith("org.w3c")
        ||  name.startsWith("javax.xml.parsers")
        ||  name.startsWith("javax.xml.transform") )
            return true;
        else
            return false;
    }
}
