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



    public static void main (String[] args) throws Exception {
        ClassLoader cl = SchemaGeneratorWrapper.class.getClassLoader();

        SchemaGeneratorClassLoader classLoader = new SchemaGeneratorClassLoader(getURLS(),cl);

        run(args, classLoader);
    }

    public static void run(String[] args, ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Options options = new Options();
        if (args.length ==0) {
            usage();
            System.exit(0);
        }
        for (String arg : args) {
            if (arg.equals("-help")) {
                usage();
                System.exit(0);
            }

            try {

                options.parseArguments(args);
            } catch (BadCommandLineException e) {
                // there was an error in the command line.
                // print usage and abort.
                if(e.getMessage()!=null) {
                    System.out.println(e.getMessage());
                    System.out.println();
                    usage();
                    System.exit(-1);
                }
            }
        }


        Class schemagenRunner = classLoader.loadClass("com.sun.tools.jxc.apt.SchemaGeneratorRunner");
        Method mainMethod = schemagenRunner.getDeclaredMethod("main", new Class[]{String[].class});
        ArrayList<String> newargs = new ArrayList(Arrays.asList(args));

        if (options.classpath != null ) {
            newargs.add("-cp");
            newargs.add(options.classpath);

       }

        String[] argsarray = newargs.toArray(new String[newargs.size()]);
        try {
            mainMethod.invoke(null,new Object[]{argsarray});
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e;
        }
    }


    /**
     * Returns a class loader that can load classes from JDK tools.jar.
     *
     */
    private static URL[]  getURLS() throws Exception {

        File jreHome = new File(System.getProperty("java.home"));
        File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );

        if (!toolsJar.exists()) {
            throw new RuntimeException("Unable to locate tools.jar. "
                    + "Expected to find it in " + toolsJar.getPath());

        }

        return new URL[]{toolsJar.toURL()};


    }

    private static void usage( ) {
        System.out.println(Messages.USAGE.format());
    }


}




