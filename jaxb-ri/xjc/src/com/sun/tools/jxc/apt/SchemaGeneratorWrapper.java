package com.sun.tools.jxc.apt;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.tools.xjc.BadCommandLineException;

/**
 * Wrapper class that will invoke the  {@link SchemaGenerator} and should
 * be called by the schemagen scripts
 *
 *
 * @author Bhakti Mehta
 */
public class SchemaGeneratorWrapper  {
    public static void main (String args[]) throws Exception {
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
                parseArguments(args);
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

        Class apt;
        try {
            apt = Class.forName("com.sun.tools.apt.Main");
        } catch (ClassNotFoundException e) {
            //Most likely this may not get executed as tools.jar
            //is there in the classpath when invoked from schemagen.sh
            //Just to make it more fool proof
            apt = getClassLoader().loadClass("com.sun.tools.apt.Main");
        }

        Method processMethod = apt.getMethod("process",
                AnnotationProcessorFactory.class,String[].class);
        System.exit((Integer)processMethod.invoke(null,SchemaGenerator.class.newInstance(),args));
    }

    /**
     * Returns a class loader that can load classes from JDK tools.jar.
     *
     */
    private static ClassLoader  getClassLoader() {
        File jreHome = new File(System.getProperty("java.home"));
        File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );
        URLClassLoader urlClassLoader ;
        try {
            urlClassLoader = new URLClassLoader(
                    new URL[]{ toolsJar.toURL() } );
            return urlClassLoader;
        } catch (MalformedURLException e) {
            throw new Error(e);
        }

    }

    private static void usage( ) {
        System.out.println(Messages.USAGE.format());
    }


    private static void parseArguments(String[] args) throws BadCommandLineException {

        for (int i = 0 ; i <args.length; i++) {
            if (args[i].charAt(0)== '-') {
                int j = parseArgument(args,i);
                if(j==0)
                    throw new BadCommandLineException(
                            Messages.UNRECOGNIZED_PARAMETER.format(args[i]));
                i += (j-1);
            }
        }

    }
    private static int parseArgument( String[] args, int i ) throws BadCommandLineException {
        if (args[i].equals("-d")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                        (Messages.NO_FILE_SPECIFIED.format()));
            File targetDir = new File(args[++i]);
            if( !targetDir.exists() )
                throw new BadCommandLineException(
                        Messages.NON_EXISTENT_FILE.format(targetDir));
            return 2;
        }

        return 0;

    }

}
