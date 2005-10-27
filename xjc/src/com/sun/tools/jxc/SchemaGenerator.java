package com.sun.tools.jxc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.tools.jxc.apt.Options;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.api.util.APTClassLoader;
import com.sun.tools.xjc.api.util.ToolsJarNotFoundException;

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
            ClassLoader classLoader = new APTClassLoader(cl,packagePrefixes);
            return run(args, classLoader);
        } catch (UnsupportedClassVersionError e) {
            System.err.println("schemagen requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
            return -1;
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
        "com.sun.tools.apt.",
        "com.sun.tools.javac.",
        "com.sun.tools.javadoc.",
        "com.sun.mirror."
    };

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
        Method mainMethod = schemagenRunner.getDeclaredMethod("main",String[].class);

        List<String> aptargs = new ArrayList<String>();

        if(hasClass(options.arguments))
            aptargs.add("-XclassesAsDecls");

        if (options.classpath != null ) {
            aptargs.add("-cp");
            aptargs.add(options.classpath);
        }

        if(options.targetDir!=null) {
            aptargs.add("-d");
            aptargs.add(options.targetDir.getPath());
        }

        aptargs.addAll(options.arguments);

        String[] argsarray = aptargs.toArray(new String[aptargs.size()]);
        return (Integer)mainMethod.invoke(null,new Object[]{argsarray});
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
        public static int main(String[] args) throws Exception {
            ClassLoader cl = Runner.class.getClassLoader();
            Class apt = cl.loadClass("com.sun.tools.apt.Main");
            Method processMethod = apt.getMethod("process",
                    new Class[]{AnnotationProcessorFactory.class, String[].class});

            return (Integer) processMethod.invoke(null, new com.sun.tools.jxc.apt.SchemaGenerator(), args);
        }
    }
}
