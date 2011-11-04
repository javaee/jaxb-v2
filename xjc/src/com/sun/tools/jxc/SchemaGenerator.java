/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jxc;

import com.sun.tools.jxc.ap.Options;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.api.util.ApClassLoader;
import com.sun.tools.xjc.api.util.ToolsJarNotFoundException;
import com.sun.xml.bind.util.Which;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.bind.JAXBContext;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLI entry-point to the schema generator.
 *
 * @author Bhakti Mehta
 */
public class SchemaGenerator {
    /**
     * Runs the schema generator.
     */
    public static void main(String[] args) throws Exception {
        System.exit(run(args));
    }

    public static int run(String[] args) throws Exception {
        try {
            ClassLoader cl = SchemaGenerator.class.getClassLoader();
            if(cl==null)    cl = ClassLoader.getSystemClassLoader();
            ClassLoader classLoader = new ApClassLoader(cl, packagePrefixes);
            return run(args, classLoader);
        } catch( ToolsJarNotFoundException e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    /**
     * List of package prefixes we want to load in the same package
     */
    private static final String[] packagePrefixes = {
        "com.sun.tools.jxc.",
        "com.sun.tools.xjc.",
        "com.sun.istack.tools.",
        "com.sun.tools.javac.",
        "com.sun.tools.javadoc.",
        "javax.annotation.processing.",
        "javax.lang.model."
    };

    /**
     * Runs the schema generator.
     *
     * @param classLoader
     *      the schema generator will run in this classLoader.
     *      It needs to be able to load annotation processing and JAXB RI classes. Note that
     *      JAXB RI classes refer to annotation processing classes. Must not be null.
     *
     * @return
     *      exit code. 0 if success.
     *
     */
    public static int run(String[] args, ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Options options = new Options();
        if (args.length ==0) {
            usage();
            return -1;
        }
        for (String arg : args) {
            if (arg.equals("-help")) {
                usage();
                return -1;
            }

            if (arg.equals("-version")) {
                System.out.println(Messages.VERSION.format());
                return -1;
            }

            if (arg.equals("-fullversion")) {
                System.out.println(Messages.FULLVERSION.format());
                return -1;
            }

        }

        try {
            options.parseArguments(args);
        } catch (BadCommandLineException e) {
            // there was an error in the command line.
            // print usage and abort.
            System.out.println(e.getMessage());
            System.out.println();
            usage();
            return -1;
        }

        Class schemagenRunner = classLoader.loadClass(Runner.class.getName());
        Method mainMethod = schemagenRunner.getDeclaredMethod("main",String[].class,File.class);

        List<String> aptargs = new ArrayList<String>();

        if(hasClass(options.arguments)) {
            aptargs.add("-XclassesAsDecls");
        }

        if (options.encoding != null) {
            aptargs.add("-encoding");
            aptargs.add(options.encoding);
        }

        // make jaxb-api.jar visible to classpath
        File jaxbApi = findJaxbApiJar();
        if(jaxbApi!=null) {
            if(options.classpath!=null) {
                options.classpath = options.classpath+File.pathSeparatorChar+jaxbApi;
            } else {
                options.classpath = jaxbApi.getPath();
            }
        }

        aptargs.add("-cp");
        aptargs.add(options.classpath);

        if(options.targetDir!=null) {
            aptargs.add("-d");
            aptargs.add(options.targetDir.getPath());
        }

        aptargs.addAll(options.arguments);

        String[] argsarray = aptargs.toArray(new String[aptargs.size()]);
        return (Integer) mainMethod.invoke(null, argsarray, options.episodeFile);
    }

    /**
     * Computes the file system path of <tt>jaxb-api.jar</tt> so that
     * Annotation Processing will see them in the <tt>-cp</tt> option.
     *
     * <p>
     * In Java, you can't do this reliably (for that matter there's no guarantee
     * that such a jar file exists, such as in Glassfish), so we do the best we can.
     *
     * @return
     *      null if failed to locate it.
     */
    private static File findJaxbApiJar() {
        String url = Which.which(JAXBContext.class);
        if(url==null)       return null;    // impossible, but hey, let's be defensive

        if(!url.startsWith("jar:") || url.lastIndexOf('!')==-1)
            // no jar file
            return null;

        String jarFileUrl = url.substring(4,url.lastIndexOf('!'));
        if(!jarFileUrl.startsWith("file:"))
            return null;    // not from file system

        try {
            File f = new File(new URL(jarFileUrl).toURI());
            if (f.exists() && f.getName().endsWith(".jar")) { // see 6510966
                return f;
            }
            f = new File(new URL(jarFileUrl).getFile());
            if (f.exists() && f.getName().endsWith(".jar")) { // this is here for potential backw. compatibility issues
                return f;
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(SchemaGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SchemaGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns true if the list of arguments have an argument
     * that looks like a class name.
     */
    private static boolean hasClass(List<String> args) {
        for (String arg : args) {
            if(!arg.endsWith(".java"))
                return true;
        }
        return false;
    }

    private static void usage( ) {
        System.out.println(Messages.USAGE.format());
    }

    public static final class Runner {
        public static boolean main(String[] args, File episode) throws Exception {

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    Arrays.asList(args),
                    null,
                    null);
            com.sun.tools.jxc.ap.SchemaGenerator r = new com.sun.tools.jxc.ap.SchemaGenerator();
            if(episode!=null)
                r.setEpisodeFile(episode);
            task.setProcessors(Collections.singleton(r));
            return task.call();
        }
    }
}
