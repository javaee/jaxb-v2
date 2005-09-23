package com.sun.tools.jxc.apt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Enumeration;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.xml.bind.util.Which;

/**
 * This stores the invocation configuration for
 * SchemaGenerator
 *
 * @author Bhakti Mehta
 */
public class Options  {

    public  String classpath  = null;


    public  void parseArguments(String[] args) throws BadCommandLineException {

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
    private  int parseArgument( String[] args, int i ) throws BadCommandLineException {
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

        if (args[i].equals("-cp") || args[i].equals("-classpath")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                        (Messages.NO_CLASSPATH_SPECIFIED.format()));
            classpath = args[++i];
            
            return 2;
        }

        return 0;

    }


}



