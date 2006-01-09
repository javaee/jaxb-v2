package com.sun.xml.bind;

import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Util {
    private Util() {}   // no instanciation

    /**
     * Gets the logger for the caller's class.
     *
     * @since 2.0
     */
    public static Logger getClassLogger() {
        try {
//            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            StackTraceElement[] trace = new Exception().getStackTrace();
            return Logger.getLogger(trace[1].getClassName());
        } catch( SecurityException _ ) {
            return Logger.getLogger("com.sun.xml.bind"); // use the default
        }
    }

    /**
     * Reads the system property value and takes care of {@link SecurityException}.
     */
    public static String getSystemProperty(String name) {
        try {
            return System.getProperty(name);
        } catch( SecurityException e ) {
            return null;
        }
    }
}
