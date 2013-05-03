/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.xjc;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * A shabby driver to invoke XJC1 or XJC2 depending on the command line switch.
 *
 * <p>
 * This class is compiled with -source 1.2 so that we can report a nice
 * user-friendly "you require Tiger" error message.
 *
 * @author Kohsuke Kawaguchi
 */
public class XJCFacade {

    private static final String JDK6_REQUIRED = "XJC requires JDK 6.0 or later. Please download it from http://www.oracle.com/technetwork/java/javase/downloads";

    public static void main(String[] args) throws Throwable {
        String v = "2.0";      // by default, we go 2.0

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-source")) {
                if (i + 1 < args.length) {
                    v = parseVersion(args[i + 1]);
                }
            }
        }

        ClassLoader oldContextCl = SecureLoader.getContextClassLoader();
        try {
            ClassLoader cl = ClassLoaderBuilder.createProtectiveClassLoader(SecureLoader.getClassClassLoader(XJCFacade.class), v);
            SecureLoader.setContextClassLoader(cl);
            Class<?> driver = cl.loadClass("com.sun.tools.xjc.Driver");
            Method mainMethod = driver.getDeclaredMethod("main", new Class[]{String[].class});
            try {
                mainMethod.invoke(null, new Object[]{args});
            } catch (InvocationTargetException e) {
                if (e.getTargetException() != null) {
                    throw e.getTargetException();
                }
            }
        } catch (UnsupportedClassVersionError e) {
            System.err.println(JDK6_REQUIRED);
        } finally {
            ClassLoader cl = SecureLoader.getContextClassLoader();
            SecureLoader.setContextClassLoader(oldContextCl);

            //close/cleanup all classLoaders but the one which loaded this class
            while (cl != null && !oldContextCl.equals(cl)) {
                if (cl instanceof Closeable) {
                    //JDK7+, ParallelWorldClassLoader
                    ((Closeable) cl).close();
                } else {
                    if (cl instanceof URLClassLoader) {
                        //JDK6 - API jars are loaded by instance of URLClassLoader
                        //so use proprietary API to release holded resources
                        try {
                            Class<?> clUtil = oldContextCl.loadClass("sun.misc.ClassLoaderUtil");
                            Method release = clUtil.getDeclaredMethod("releaseLoader", URLClassLoader.class);
                            release.invoke(null, cl);
                        } catch (ClassNotFoundException ex) {
                            //not Sun JDK 6, ignore
                            System.err.println(JDK6_REQUIRED);
                        }
                    }
                }
                cl = SecureLoader.getParentClassLoader(cl);
            }
        }
    }

    public static String parseVersion(String version) {
        // no other versions supported as of now
        return "2.0";
    }
}
