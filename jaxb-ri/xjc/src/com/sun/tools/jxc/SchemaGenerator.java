package com.sun.tools.jxc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URL;
import java.io.File;

import com.sun.tools.jxc.apt.*;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.mirror.apt.AnnotationProcessorFactory;

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
        try {
            ClassLoader cl = SchemaGenerator.class.getClassLoader();
            ClassLoader classLoader = new SchemaGeneratorClassLoader(cl,getToolsJar());
            System.exit(run(args, classLoader));
        } catch (UnsupportedClassVersionError e) {
            System.err.println("schemagen requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
        }
    }

    /**
     * Runs the schema generator.
     *
     * @param classLoader
     *      the schema generator will run in this classLoader.
     *      It needs to be able to load APT and JAXB RI classes. Note that
     *      JAXB RI classes refer to APT classes. Must not be null.
     *
     * @return
     *      exit code. 0 if success.
     *
     */
    public static int run(String[] args, ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Options options = new Options();
        if (args.length ==0) {
            usage();
            return 0;
        }
        for (String arg : args) {
            if (arg.equals("-help")) {
                usage();
                return 0;
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
        }


        Class schemagenRunner = classLoader.loadClass(Runner.class.getName());
        Method mainMethod = schemagenRunner.getDeclaredMethod("main",String[].class);
        List<String> newargs = new ArrayList<String>(Arrays.asList(args));

        if (options.classpath != null ) {
            newargs.add("-cp");
            newargs.add(options.classpath);
        }

        String[] argsarray = newargs.toArray(new String[newargs.size()]);
        return ((Integer)mainMethod.invoke(null,new Object[]{argsarray}));
    }


    /**
     * Returns a class loader that can load classes from JDK tools.jar.
     *
     */
    private static URL getToolsJar() throws Exception {

        File jreHome = new File(System.getProperty("java.home"));
        File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );

        if (!toolsJar.exists()) {
            throw new RuntimeException("Unable to locate tools.jar. "
                    + "Expected to find it in " + toolsJar.getPath());
        }

        return toolsJar.toURL();
    }

    private static void usage( ) {
        System.out.println(Messages.USAGE.format());
    }

    public static final class Runner {
        public static int main(String[] args) throws Exception {
            ClassLoader cl = Runner.class.getClassLoader();
            Class apt = cl.loadClass("com.sun.tools.apt.Main");
            Method processMethod = apt.getMethod("process",
                    new Class[]{AnnotationProcessorFactory.class, String[].class});

            return (Integer) processMethod.invoke(null, new com.sun.tools.jxc.apt.SchemaGenerator(), args);
        }
    }
}
