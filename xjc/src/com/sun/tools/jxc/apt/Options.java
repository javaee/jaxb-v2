package com.sun.tools.jxc.apt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sun.tools.xjc.BadCommandLineException;

/**
 * This stores the invocation configuration for
 * SchemaGenerator
 *
 * @author Bhakti Mehta
 */
public class Options  {

    // honor CLASSPATH environment variable, but it will be overrided by -cp
    public String classpath = System.getenv("CLASSPATH");

    public File targetDir = null;

    public File episodeFile = null;

    public final List<String> arguments = new ArrayList<String>();

    public void parseArguments(String[] args) throws BadCommandLineException {
        for (int i = 0 ; i <args.length; i++) {
            if (args[i].charAt(0)== '-') {
                int j = parseArgument(args,i);
                if(j==0)
                    throw new BadCommandLineException(
                            Messages.UNRECOGNIZED_PARAMETER.format(args[i]));
                i += j;
            } else {
                arguments.add(args[i]);
            }
        }
    }

    private int parseArgument( String[] args, int i ) throws BadCommandLineException {
        if (args[i].equals("-d")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                        (Messages.OPERAND_MISSING.format(args[i])));
            targetDir = new File(args[++i]);
            if( !targetDir.exists() )
                throw new BadCommandLineException(
                        Messages.NON_EXISTENT_FILE.format(targetDir));
            return 1;
        }

        if (args[i].equals("-episode")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                        (Messages.OPERAND_MISSING.format(args[i])));
            episodeFile = new File(args[++i]);
            return 1;
        }

        if (args[i].equals("-cp") || args[i].equals("-classpath")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                        (Messages.OPERAND_MISSING.format(args[i])));
            classpath = args[++i];
            
            return 1;
        }

        return 0;

    }


}



