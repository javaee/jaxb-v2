package com.sun.tools.jxc.apt;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import com.sun.tools.xjc.BadCommandLineException;

/**
 * Wrapper class that will invoke the  {@link SchemaGeneratorRunner} and should
 * be called by the schemagen scripts
 * It checks for existence of tools.jar and sets the
 * {@link SchemaGeneratorClassLoader}
 *
 *
 * @author Bhakti Mehta
 */
public class SchemaGeneratorWrapper  {

    /**
     * Runs the schema generator.
     */
    public static void main (String[] args) throws Exception {
        ClassLoader cl = SchemaGeneratorWrapper.class.getClassLoader();

        SchemaGeneratorClassLoader classLoader = new SchemaGeneratorClassLoader(cl,getToolsJar());

        System.exit(run(args, classLoader));
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


        Class schemagenRunner = classLoader.loadClass(SchemaGeneratorRunner.class.getName());
        Method mainMethod = schemagenRunner.getDeclaredMethod("main", String[].class);
        ArrayList<String> newargs = new ArrayList<String>(Arrays.asList(args));

        if (options.classpath != null ) {
            newargs.add("-cp");
            newargs.add(options.classpath);
        }

        String[] argsarray = newargs.toArray(new String[newargs.size()]);
        return (Integer)mainMethod.invoke(null,new Object[]{argsarray});
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


}




